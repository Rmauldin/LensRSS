package com.example.ryan.newsviewer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.jsoup.Jsoup;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RssFeedModel implements Parcelable{
    private String htmlDetails;
    private String title;
    private String link;
    private URI uri;
    private String content;
    private String details;
    private String domain;
    private String displayDate;
    private String author;
    private double polarity;
    private double subjectivity;
    private String leaning;
    private Date date;
    private String thumbnail_url;
    //private Bitmap image;
    private byte[] image;
    // loaded is a String in order to be parceable
    private String loaded;


    public RssFeedModel(String title, String link, String details, String date){
        this.title = title;
        if( !link.startsWith("http://") && !link.startsWith("https://")){
            this.link = "http://" + link;
        }else {
            this.link = link;
        }
        this.image = new byte[0];
        this.thumbnail_url = null;
        this.htmlDetails = details;
        this.details = cleanDetails(details);
        getURI(link);
        getDate(date);
        loaded = "false";
    }

    public RssFeedModel(Parcel in){
        this.htmlDetails = in.readString();
        this.title = in.readString();
        this.link = in.readString();
        this.content = in.readString();
        this.details = in.readString();
        this.domain = in.readString();
        this.displayDate = in.readString();
        this.author = in.readString();
        this.thumbnail_url = in.readString();
        this.loaded = in.readString();
        this.polarity = Double.parseDouble(in.readString());
        this.subjectivity = Double.parseDouble(in.readString());
        this.leaning = in.readString();
    }

    public RssFeedModel(RssFeedModel returnItem) {
        this.htmlDetails = returnItem.htmlDetails;
        this.title = returnItem.title;
        this.link = returnItem.link;
        this.content = returnItem.content;
        this.details = returnItem.details;
        this.domain = returnItem.domain;
        this.displayDate = returnItem.displayDate;
        this.author = returnItem.author;
        this.thumbnail_url = returnItem.thumbnail_url;
        this.loaded = returnItem.loaded;
        this.image = returnItem.image;
        this.polarity = returnItem.polarity;
        this.subjectivity = returnItem.subjectivity;
        this.leaning = returnItem.leaning;
    }

    public void setThumbnail(String img_url){
        this.thumbnail_url = img_url;
    }

    public void setAuthor(String author){
        this.author = author;
    }

    public String getAuthor(){
        return author;
    }

    public void setImage(Bitmap photo) {
        if(photo == null) return;
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 25, bs);
        image = bs.toByteArray();
    }

    public void setDate(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY");
        this.date = date;
        this.displayDate = sdf.format(date);
    }

    public Date getDate(){
        return date;
    }

    public String getDisplayDate(){
        return displayDate;
    }

    private String cleanDetails(String details){
        return Jsoup.parse(details).text();
    }

    private void getURI(String link){
        try{
            this.uri = new URI(link);
        }catch(NullPointerException e){
            Log.d("RssFeedModel", e.toString());
            this.uri = null;
        }catch(URISyntaxException e){
            Log.d("RssFeedModel", e.toString());
            this.uri = null;
        }
        if(uri != null){
            domain = uri.getHost();
            if(domain.startsWith("www."))
                domain = domain.substring(4);
        }else{
            domain = link;
        }
    }

    private String customParseDate(String date) throws customParseDateException {
        StringBuilder builder = new StringBuilder();
        String month, day, year;
        try {
            month = getCustomMonth(date);
            day = getCustomDay(date);
            year = getCustomYear(date);
        }catch(getMonthException e){
            Log.d("customParseDate", "Could not parse for month");
            throw new customParseDateException();
        }catch(getDayException e){
            Log.d("customParseDate", "Could not parse for day");
            throw new customParseDateException();
        }catch(getYearException e){
            Log.d("customParseDate", "Could not parse for year");
            throw new customParseDateException();
        }
        return month + "/" + day + "/" + year;
    }

    private String getCustomYear(String date) throws getYearException{
        for(String s : date.split(" ")){
            if(Character.isDigit(s.charAt(0)) && s.length() == 4){
                return s;
            }
        }
        throw new getYearException();
    }

    private String getCustomDay(String date) throws getDayException {
        for(String s : date.split(" ")){
            if(Character.isDigit(s.charAt(0))){
                return s;
            }
        }
        throw new getDayException();
    }

    private String getCustomMonth(String date) throws getMonthException {
        for(String s : date.split(" ")){
            s = s.toLowerCase();
            if(s.startsWith("jan")){
                return "1";
            }else if(s.startsWith("feb")){
                return "2";
            }else if(s.startsWith("mar")){
                return "3";
            }else if(s.startsWith("apr")){
                return "4";
            }else if(s.startsWith("may")){
                return "5";
            }else if(s.startsWith("jun")){
                return "6";
            }else if(s.startsWith("jul")){
                return "7";
            }else if(s.startsWith("aug")){
                return "8";
            }else if(s.startsWith("sep")){
                return "9";
            }else if(s.startsWith("oct")){
                return "10";
            }else if(s.startsWith("nov")){
                return "11";
            }else if(s.startsWith("dec")){
                return "12";
            }
        }
        throw new getMonthException();
    }

    private void getDate(String date){
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        try {
            formatter.parse(date);
            this.displayDate = formatter.format(new Date());
        } catch (ParseException e) {
            Log.d("RssFeedModel", e.toString());
            try{
                this.displayDate = customParseDate(date);
            }catch (customParseDateException ec){
                Log.d("RssFeedModel", "Could not custom parse date");
                this.displayDate = date;
            }
        }
    }

    public void setContent(String content){
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public Bitmap getImage() {
        if(image == null) return null;
        if(image.length == 0) return null;
        return BitmapFactory.decodeByteArray(image, 0,image.length);
    }

    public String getContent() {
        return content;
    }

    public String getLink() {
        return link;
    }

    public String getDetails() {
        return details;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(htmlDetails);
        dest.writeString(title);
        dest.writeString(link);
        dest.writeString(content);
        dest.writeString(details);
        dest.writeString(domain);
        dest.writeString(displayDate);
        dest.writeString(author);
        dest.writeString(thumbnail_url);
        dest.writeString(loaded);
        dest.writeString(String.valueOf(polarity));
        dest.writeString(String.valueOf(subjectivity));
        dest.writeString(leaning);
    }

    public static final Parcelable.Creator<RssFeedModel> CREATOR = new Parcelable.Creator<RssFeedModel>() {

        public RssFeedModel createFromParcel(Parcel in) {
            return new RssFeedModel(in);
        }

        public RssFeedModel[] newArray(int size) {
            return new RssFeedModel[size];
        }
    };

    public void setLoaded(String loaded) {
        this.loaded = loaded;
    }

    public String getLoaded() {
        return loaded;
    }

    public void setHtmlDetails(String htmlDetails) {
        this.htmlDetails = htmlDetails;
    }

    public String getHtmlDetails() {
        return htmlDetails;
    }

    public String getThumbnailUrl() {
        return thumbnail_url;
    }

    public void setPolarity(double polarity) {
        this.polarity = polarity;
    }

    public double getPolarity(){
        return polarity;
    }

    public void setSubjectivity(double subjectivity){
        this.subjectivity = subjectivity;
    }

    public double getSubjectivity(){
        return this.subjectivity;
    }

    public void setImage(String image) {

    }

    public void setLeaning(String leaning) {
        if(leaning.length() > 0)
            this.leaning = leaning.substring(0, 1).toUpperCase() + leaning.substring(1);
        else
            this.leaning = leaning;
    }

    public String getLeaning(){
        return this.leaning;
    }

    private class getMonthException extends Throwable {
    }

    private class getDayException extends Throwable {
    }

    private class getYearException extends Throwable {
    }

    private class customParseDateException extends Throwable {
    }
}
