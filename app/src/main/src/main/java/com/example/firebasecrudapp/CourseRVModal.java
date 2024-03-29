package com.example.firebasecrudapp;

import android.os.Parcel;
import android.os.Parcelable;

public class CourseRVModal implements Parcelable {
    //creating variables for our different fields.
    private String courseName;
    private String courseDescription;
    private String coursePrice;
    private String bestSuitedFor;
//    private String courseLink;
    private String courseId;


    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }


    //creating an empty constructor.
    public CourseRVModal() {

    }

    protected CourseRVModal(Parcel in) {
        courseName = in.readString();
        courseId = in.readString();
        courseDescription = in.readString();
        coursePrice = in.readString();
        bestSuitedFor = in.readString();
//        courseLink = in.readString();
    }

    public static final Creator<CourseRVModal> CREATOR = new Creator<CourseRVModal>() {
        @Override
        public CourseRVModal createFromParcel(Parcel in) {
            return new CourseRVModal(in);
        }

        @Override
        public CourseRVModal[] newArray(int size) {
            return new CourseRVModal[size];
        }
    };

    //creating getter and setter methods.
    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseDescription() {
        return courseDescription;
    }

    public void setCourseDescription(String courseDescription) {
        this.courseDescription = courseDescription;
    }

    public String getCoursePrice() {
        return coursePrice;
    }

    public void setCoursePrice(String coursePrice) {
        this.coursePrice = coursePrice;
    }

    public String getBestSuitedFor() {
        return bestSuitedFor;
    }

    public void setBestSuitedFor(String bestSuitedFor) {
        this.bestSuitedFor = bestSuitedFor;
    }


//    public String getCourseLink() {
//        return courseLink;
//    }
//
//    public void setCourseLink(String courseLink) {
//        this.courseLink = courseLink;
//    }


    public CourseRVModal(String courseId, String courseName, String courseDescription, String coursePrice, String bestSuitedFor) {
        this.courseName = courseName;
        this.courseId = courseId;
        this.courseDescription = courseDescription;
        this.coursePrice = coursePrice;
        this.bestSuitedFor = bestSuitedFor;
//        this.courseLink = courseLink;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(courseName);
        dest.writeString(courseId);
        dest.writeString(courseDescription);
        dest.writeString(coursePrice);
        dest.writeString(bestSuitedFor);
//        dest.writeString(courseLink);
    }
}
