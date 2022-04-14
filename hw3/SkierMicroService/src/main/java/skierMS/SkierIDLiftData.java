package skierMS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class SkierIDLiftData {
    private List<SkierRide> skierRideList;
    public SkierIDLiftData(){
        List<SkierRide> list = new ArrayList<SkierRide>();
        skierRideList = Collections.synchronizedList(list);
    }

    public List<SkierRide> addToSkierIDData(SkierRide skierRideObj){
        this.skierRideList.add(skierRideObj);
        return skierRideList;
    }

}
