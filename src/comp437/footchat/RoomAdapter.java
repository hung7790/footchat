package comp437.footchat;

import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

class RoomAdapter extends BaseAdapter {

	Context context;
	List<ChatRoomItem> data;
	private static LayoutInflater inflater = null;

	public RoomAdapter(Context context, List<ChatRoomItem> data) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.data = data;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View vi = convertView;
		if (vi == null)
			vi = inflater.inflate(R.layout.roomitem, null);
		TextView roomname = (TextView) vi.findViewById(R.id.txt_roomname);
		TextView status = (TextView) vi.findViewById(R.id.txt_status);

		roomname.setText(data.get(position).description);
		roomname.setTag(data.get(position).chatroomId);
		status.setText((data.get(position).status));

		if (data.get(position).favour) {
			roomname.setTextColor(Color.parseColor("#FF0000"));
		}

		return vi;
	}

}
