package com.android.countdowntimer.home;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.countdowntimer.R;
import com.android.countdowntimer.utils.DateTimeUtils;
import com.github.vipulasri.timelineview.TimelineView;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

import cn.iwgang.countdownview.CountdownView;
import cn.iwgang.countdownview.DynamicConfig;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder> implements EventTouchHelperListener {

    private EventItemActionListener mEventItemActionListener;
    private final LayoutInflater mInflater;
    private List<Event> mEvents;
    private Context mContext;

    @Override
    public void onItemSwipeToStart(int position) {
        mEventItemActionListener.onItemSwiped(mEvents.get(position).getId());
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private Event mEvent;
        private TextView mTitle;
        private View mTimerContainer;
        private TimerView mTimer;
        private TextView mTimerState;
        private MaterialCardView mCard;
        private TextView mDueDate;
        private TimelineView mMarker;

        private ViewHolder(View itemView, int type) {
            super(itemView);

            mCard = itemView.findViewById(R.id.event_card);
            mTitle = itemView.findViewById(R.id.event_title);
            mTimerContainer = itemView.findViewById(R.id.event_timer_container);
            mTimer = itemView.findViewById(R.id.event_timer);
            mTimerState = itemView.findViewById(R.id.event_timer_state);
            mDueDate = itemView.findViewById(R.id.event_due_date);
            mMarker = itemView.findViewById(R.id.event_marker);
            mMarker.initLine(type);
        }

        private void bind(Event event) {
            mEvent = event;
            mDueDate.setText(DateTimeUtils.longToString(mEvent.getEndDate(), DateTimeUtils.TIME) + "\n" +
                    DateTimeUtils.longToString(mEvent.getEndDate(), DateTimeUtils.DATE));
            refreshNormal(mEvent);
            mTitle.setText(event.getTitle());
            if(event.getEndDate() < System.currentTimeMillis()) {
                mTimer.stop();
                mEventItemActionListener.onItemSwiped(event.getId());
            }
        }

        private void refreshNormal(final Event event) {
            activeEvent();
            mTimer.start(mEvent.getEndDate() - System.currentTimeMillis());
            toTealTheme();
            mTimerState.setText(R.string.timer_state_to_reach);
            mTimer.setOnCountdownEndListener(new CountdownView.OnCountdownEndListener() {
                @Override
                public void onEnd(CountdownView cv) {
                    mEventItemActionListener.onItemSwiped(event.getId());
                }
            });
        }

        private void activeEvent() {
            mTitle.getPaint().setFlags(Paint.ANTI_ALIAS_FLAG);
            mDueDate.setPadding(0, 0, 0, 0);
            mTimerContainer.setVisibility(View.VISIBLE);
        }

        private void toTealTheme() {
            mTitle.setTextColor(mContext.getResources().getColor(R.color.white));
            mDueDate.setTextColor(mContext.getResources().getColor(R.color.yellow_300));
            mCard.setCardBackgroundColor(mContext.getResources().getColor(R.color.teal_600));
            mTimerState.setTextColor(mContext.getResources().getColor(R.color.white_secondary));
            DynamicConfig.Builder dynamicConfigBuilder = new DynamicConfig.Builder();
            dynamicConfigBuilder
                    .setTimeTextColor(mContext.getResources().getColor(R.color.white))
                    .setSuffixTextColor(mContext.getResources().getColor(R.color.white_secondary))
                    .setShowSecond(true)
                    .setShowMinute(true);
            mTimer.dynamicShow(dynamicConfigBuilder.build());
        }
    }

    EventsAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.item_event, parent, false);
        return new ViewHolder(itemView, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        if (mEvents != null) {
            final Event current = mEvents.get(position);
            holder.bind(current);

            if (mEventItemActionListener != null) {
                holder.mCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mEventItemActionListener.onItemClicked(current.getId());
                    }
                });
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return TimelineView.getTimeLineViewType(position, getItemCount());
    }

    @Override
    public int getItemCount() {
        if (mEvents != null) {
            return mEvents.size();
        } else {
            return 0;
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        holder.mTimer.stop();
    }

    public void setEvents(List<Event> events) {
        mEvents = events;
        notifyDataSetChanged();
    }

    public void setEventItemActionListener(EventItemActionListener listener) {
        mEventItemActionListener = listener;
    }
}
