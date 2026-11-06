package com.example.registrationform;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/**
 * Immutable data class carrying the registration data to the summary screen.
 * Password is intentionally excluded for security.
 */
public final class RegistrationData implements Parcelable {

    public static final String EXTRA_KEY = "registration_data";

    public final String name;
    public final String email;
    public final Long dobUtcMillis;

    public RegistrationData(String name, String email, Long dobUtcMillis) {
        this.name = name;
        this.email = email;
        this.dobUtcMillis = dobUtcMillis;
    }

    private RegistrationData(Parcel in) {
        name = in.readString();
        email = in.readString();
        long dob = in.readLong();
        dobUtcMillis = dob == -1 ? null : dob;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(email);
        dest.writeLong(dobUtcMillis != null ? dobUtcMillis : -1);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RegistrationData> CREATOR = new Creator<RegistrationData>() {
        @Override
        public RegistrationData createFromParcel(Parcel in) {
            return new RegistrationData(in);
        }

        @Override
        public RegistrationData[] newArray(int size) {
            return new RegistrationData[size];
        }
    };
}