package cs6650hw1.hw1server;

public class Resort {
    public String resortName;
    public int resortID;

    public Resort(String resortName, int resortID) {
        this.resortName = resortName;
        this.resortID = resortID;
    }

    public String getResortName() {
        return resortName;
    }

    public void setResortName(String resortName) {
        this.resortName = resortName;
    }

    public int getResortID() {
        return resortID;
    }

    public void setResortID(int resortID) {
        this.resortID = resortID;
    }
}
