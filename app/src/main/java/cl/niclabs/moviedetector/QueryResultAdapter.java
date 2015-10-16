package cl.niclabs.moviedetector;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

import cl.niclabs.moviedetector.descriptors.http.Detection;

/**
 * Created by felipe on 16-10-15.
 */
public class QueryResultAdapter extends ArrayAdapter<Detection> {

    private Context context;
    private List<Detection> detections;

    public QueryResultAdapter(Context context, int resource, List<Detection> objects) {
        super(context, resource, objects);
        this.context = context;
        this.detections = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//        return super.getView(position, convertView, parent);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.query_result_layout, parent, false);
        TextView title = (TextView) rowView.findViewById(R.id.result_title);
        TextView score = (TextView) rowView.findViewById(R.id.result_score);

        Detection detection = detections.get(position);
        title.setText(detection.getReference());
        score.setText(detection.getScore()+"");
        // change the icon for Windows and iPhone

        return rowView;
    }
}
