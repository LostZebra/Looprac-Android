package looprac.app;

import com.google.android.gms.maps.model.Marker;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by XiaoYong on 2014/6/22.
 */
public class SerializableList implements Serializable{

    private ArrayList<CarpoolInfo> list;
    public ArrayList<CarpoolInfo> getList()
    {
        return this.list;
    }
    public void setList(ArrayList<CarpoolInfo> list)
    {
        this.list = list;
    }
}
