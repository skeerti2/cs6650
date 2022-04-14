package skierMS;

import com.google.gson.Gson;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SkierRide implements Serializable {
    private int time;
    private int liftID;
    private int waitTime;
    private int resortID;
    private int seasonID;
    private int dayID;
    private int skierID;

    public SkierRide(int time, int liftID, int waitTime) {
        this.time = time;
        this.liftID = liftID;
        this.waitTime = waitTime;
    }

    public void setResortID(int resortID) {
        this.resortID = resortID;
    }

    public void setSeasonID(int seasonID) {
        this.seasonID = seasonID;
    }

    public void setDayID(int dayID) {
        this.dayID = dayID;
    }

    public void setSkierID(int skierID) {
        this.skierID = skierID;
    }


    public int getTime() {
        return time;
    }

    public int getLiftID() {
        return liftID;
    }

    public int getWaitTime() {
        return waitTime;
    }

    public int getResortID() {
        return resortID;
    }

    public int getSeasonID() {
        return seasonID;
    }

    public int getDayID() {
        return dayID;
    }

    public int getSkierID() {
        return skierID;
    }

    public byte[] getBytes() {
        byte[] bytes;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try{
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);
            oos.flush();
            oos.reset();
            bytes = baos.toByteArray();
            oos.close();
            baos.close();
        } catch(IOException e){
            bytes = new byte[] {};
            Logger.getLogger("bsdlog").log(Level.ALL, "unable to write to output stream" + e);
        }
        return bytes;
    }

    public static SkierRide fromBytes(byte[] body) {
        SkierRide obj = null;
        //try {
            //ByteArrayInputStream bis = new ByteArrayInputStream(body);
            String s = new String(body, StandardCharsets.UTF_8);
            //ObjectInputStream ois = new ObjectInputStream(bis)
            Gson gson = new Gson();
            obj = gson.fromJson(s, SkierRide.class);
//            obj = (SkierRide) ois.readObject();
//            ois.close();
//            bis.close();
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
//        catch (ClassNotFoundException ex) {
//            ex.printStackTrace();
        //}
        return obj;
    }

    @Override
    public String toString() {
        return "SkierRide{" +
                "time=" + time +
                ", liftID=" + liftID +
                ", waitTime=" + waitTime +
                ", resortID=" + resortID +
                ", seasonID=" + seasonID +
                ", dayID=" + dayID +
                ", skierID=" + skierID +
                '}';
    }
}
