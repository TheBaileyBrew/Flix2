package com.thebaileybrew.flix2.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "moviecredits", indices = {@Index("movie_id")},
        foreignKeys = @ForeignKey(entity = Movie.class, parentColumns = "movie_id", childColumns = "movie_id",
        onDelete = CASCADE))
public class Credit {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int creditID;

    @ColumnInfo(name = "movie_id")
    private String filmID;

    @ColumnInfo(name = "character_name")
    private  String creditCharacterName;

    @ColumnInfo(name = "actor_name")
    private String creditActorName;

    @ColumnInfo(name = "image_path")
    private String creditPath;


    public int getCreditID() {
        return creditID;
    }

    public void setCreditID(int creditID) {
        this.creditID = creditID;
    }

    public String getFilmID() {
        return filmID;
    }

    public void setFilmID(String filmID) {
        this.filmID = filmID;
    }

    public String getCreditCharacterName() {
        return creditCharacterName;
    }

    public void setCreditCharacterName(String creditCharacterName) {
        this.creditCharacterName = creditCharacterName;
    }

    public String getCreditActorName() {
        return creditActorName;
    }

    public void setCreditActorName(String creditActorName) {
        this.creditActorName = creditActorName;
    }

    public String getCreditPath() {
        return creditPath;
    }

    public void setCreditPath(String creditPath) {
        this.creditPath = creditPath;
    }
}
