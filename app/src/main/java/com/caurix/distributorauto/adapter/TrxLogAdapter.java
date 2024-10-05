package com.caurix.distributorauto.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.caurix.distributor.R;
import com.caurix.distributorauto.DistributorDB;
import com.caurix.distributorauto.TRX_STATUS;
import com.caurix.distributorauto.model.TrxLog;
import com.caurix.distributorauto.model.TrxLogGroupItem;
import com.caurix.distributorauto.expandablerecyclerview.MultiTypeExpandableRecyclerViewAdapter;
import com.caurix.distributorauto.expandablerecyclerview.models.ExpandableGroup;
import com.caurix.distributorauto.expandablerecyclerview.models.ExpandableListPosition;
import com.caurix.distributorauto.expandablerecyclerview.viewholders.ChildViewHolder;
import com.caurix.distributorauto.expandablerecyclerview.viewholders.GroupViewHolder;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.view.LayoutInflater.from;

public class TrxLogAdapter extends MultiTypeExpandableRecyclerViewAdapter<TrxLogAdapter.TrxLogGroupViewHolder, ChildViewHolder> {

    public static final int HEADER_VIEW_TYPE = 0;
    public static final int ITEM_VIEW_TYPE = 1;


    private static Context mContext;
    private List<TrxLogGroupItem> trxLogGroupItems;

    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onClickGroupPosition(View view, int position, int childIndex);
    }

    public TrxLogAdapter(List<TrxLogGroupItem> groups, Context context) {
        super(groups);
        this.trxLogGroupItems = groups;
        if(trxLogGroupItems.size() >0)Collections.sort(trxLogGroupItems, new Comparator<TrxLogGroupItem>() {
            @Override
            public int compare(final TrxLogGroupItem object1, final TrxLogGroupItem object2) {
                return object1.getName().compareTo(object2.getName());
            }
        });
        this.mContext = context;
    }

    @Override
    public TrxLogGroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = from(parent.getContext()).inflate(R.layout.item_group, parent, false);
        return new TrxLogGroupViewHolder(view);
    }

    @Override
    public ChildViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case HEADER_VIEW_TYPE:
                View header = from(parent.getContext()).inflate(R.layout.item_header_section, parent, false);
                return new HeaderChildViewHolder(header);
            case ITEM_VIEW_TYPE:
                View position = from(parent.getContext()).inflate(R.layout.item_history, parent, false);
                return new PositionChildViewHolder(position);
            default:
                throw new IllegalArgumentException("Invalid viewType");
        }
    }

    @Override
    public void onBindChildViewHolder(final ChildViewHolder holder, int flatPosition, ExpandableGroup group,
                                      final int childIndex) {
        PositionChildViewHolder viewHolderPosition = (PositionChildViewHolder) holder;
        TrxLog trxLog = ((TrxLogGroupItem) group).getItems().get(childIndex);
        viewHolderPosition.tvRStatus.setText(trxLog.getTrxStatus());
        if (viewHolderPosition.tvRStatus.getText().toString().equalsIgnoreCase(TRX_STATUS.OK.toString())) {
            viewHolderPosition.tvRStatus.setTextColor(Color.GREEN);
        } else if (viewHolderPosition.tvRStatus.getText().toString().equalsIgnoreCase(TRX_STATUS.REJET.toString())) {
            viewHolderPosition.tvRStatus.setTextColor(Color.RED);
        } else if (viewHolderPosition.tvRStatus.getText().toString().equalsIgnoreCase(TRX_STATUS.PENDING.toString())) {
            viewHolderPosition.tvRStatus.setTextColor(Color.BLUE);
        }
        viewHolderPosition.tvRTargetNumber.setText(trxLog.getTrxTargetNumber());
        viewHolderPosition.tvRDate.setText(trxLog.getTrxDateTime());
        viewHolderPosition.tvRAmount.setText(trxLog.getTrxAmount());
        viewHolderPosition.tvRTrxID.setText(trxLog.getTrxNotes());
        viewHolderPosition.tvhTrxType.setText(trxLog.getTrxType());


    }


    @Override
    public void onBindGroupViewHolder(final TrxLogGroupViewHolder holder, int flatPosition,
                                      ExpandableGroup group) {
        final TrxLogGroupItem item = ((TrxLogGroupItem) group);
        holder.name.setText(item.getName());
        holder.phone.setText(item.getPhone());
        holder.itemCount.setText(String.valueOf(group.getItemCount()));
    }

    @Override
    public int getChildViewType(int position, ExpandableGroup group, int childIndex) {
        return ITEM_VIEW_TYPE;
    }

    @Override
    public boolean isGroup(int viewType) {
        return viewType == ExpandableListPosition.GROUP;
    }

    @Override
    public boolean isChild(int viewType) {
        return viewType == HEADER_VIEW_TYPE || viewType == ITEM_VIEW_TYPE;
    }

    static class TrxLogGroupViewHolder extends GroupViewHolder {
        TextView name, phone,itemCount;
        ImageView icon;

        public TrxLogGroupViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            phone = (TextView) itemView.findViewById(R.id.phone);
            itemCount = (TextView) itemView.findViewById(R.id.count);
            icon = (ImageView) itemView.findViewById(R.id.icon);

        }

        @Override
        public void expand() {
            icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_arrow_up));

        }

        @Override
        public void collapse() {
            icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_drop_down));

        }

    }

    class PositionChildViewHolder extends ChildViewHolder {
        TextView tvRTargetNumber, tvRStatus, tvRDate, tvRAmount, tvRTrxID, tvhTrxType;

        PositionChildViewHolder(View itemView) {
            super(itemView);
            tvRTargetNumber = (TextView) itemView.findViewById(R.id.txtTargetNumber);
            tvRStatus = (TextView) itemView.findViewById(R.id.txtStatus);
            tvRDate = (TextView) itemView.findViewById(R.id.txtDate);
            tvRAmount = (TextView) itemView.findViewById(R.id.txtAmount);
            tvRTrxID = (TextView) itemView.findViewById(R.id.txtTrxID);
            tvhTrxType = (TextView) itemView.findViewById(R.id.txtTrxType);

        }

    }

    class HeaderChildViewHolder extends ChildViewHolder {

        HeaderChildViewHolder(View itemView) {
            super(itemView);
        }

    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    private int getPositionParent(TrxLogGroupItem groupItem) {
        for (int i = 0; i < trxLogGroupItems.size(); i++) {
            if (trxLogGroupItems.get(i).equals(groupItem)) {
                return i;
            }
        }
        return 0;
    }

}
