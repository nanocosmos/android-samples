package net.nanocosmos.bintu.demo.views;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Handler;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nanocosmos GmbH (c)
 */
public class MetadataListview extends ListView {
    public static final float NON_EXPANDED_HEIGHT = 150f;
    public static final float EXPANDED_HEIGHT = 500f;
    private List<JSONObject> metadata;
    private boolean isScrolling = false;
    private boolean isExpanded = false;
    private Adapter adapter;
    int clicks = 0;

    public MetadataListview(Context context) {
        super(context);
        setup();
    }


    public MetadataListview(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public MetadataListview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    private void setup() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) NON_EXPANDED_HEIGHT);
        setPadding(16, 16, 16, 16);
        setBackgroundResource(android.R.drawable.toast_frame);
        setVisibility(View.GONE);
        setAlpha(0.8f);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        setLayoutParams(params);
        setOnScrollListener(new ScrollListener());
        setOnItemClickListener(new OnItemClickListener() {


            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                clicks++;
                Handler handler = new Handler();
                Runnable r = new Runnable() {

                    @Override
                    public void run() {
                        clicks = 0;
                    }
                };

                if (clicks == 1) {
                    //Single click
                    handler.postDelayed(r, 250);
                } else if (clicks == 2) {
                    //Double click
                    clicks = 0;
                    MetadataListview.this.post(new Runnable() {
                        @Override
                        public void run() {
                            MetadataListview.this.toggleSize();
                        }
                    });
                }
            }

        });
        metadata = new ArrayList<JSONObject>();
        adapter = new Adapter(metadata);
        this.setAdapter(adapter);
    }

    private ValueAnimator createAnimator() {
        float targetHeight = EXPANDED_HEIGHT;
        float oldHeight = NON_EXPANDED_HEIGHT;
        if (isExpanded) {
            targetHeight = NON_EXPANDED_HEIGHT;
            oldHeight = EXPANDED_HEIGHT;

        }
        ValueAnimator animator = ValueAnimator.ofFloat(oldHeight, targetHeight);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                updateHeight((Float) animation.getAnimatedValue());
            }
        });
        animator.setInterpolator(new FastOutSlowInInterpolator());
        return animator;
    }

    private void updateHeight(float viewHeight) {

        ViewGroup.LayoutParams lp = getLayoutParams();
        lp.height = (int) viewHeight;
        setLayoutParams(lp);


    }


    private void toggleSize() {
        Animator anm = createAnimator();
        anm.setDuration(300);
        anm.start();
        this.isExpanded = !this.isExpanded;
        if (!this.isExpanded) {
            setSelection(metadata.size() - 1);
        }
    }

    public void setNewMetadata(JSONObject newData) {
        adapter.add(newData);
        setVisibility(View.VISIBLE);
        if (!isExpanded && clicks == 0) {

            setSelection(metadata.size() - 1);

        }
    }


    public void setIsScrolling(boolean isScrolling) {
        this.isScrolling = isScrolling;
    }


    private class Adapter implements ListAdapter {
        List<JSONObject> metadata;
        private List<DataSetObserver> observer;

        public Adapter(List<JSONObject> ref) {
            this.metadata = ref;
            this.observer = new ArrayList<DataSetObserver>();
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            if (metadata.size() > position) {
                return true;
            }
            return false;
        }

        public void add(JSONObject newObj) {
            metadata.add(newObj);
            for (DataSetObserver observer : this.observer) {
                observer.onChanged();
            }
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {
            this.observer.add(observer);
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            this.observer.remove(observer);
        }

        @Override
        public int getCount() {
            return metadata.size();
        }

        @Override
        public Object getItem(int position) {
            if (metadata.size() <= position) {
                return 0;
            }
            return metadata.get(position);
        }

        @Override
        public long getItemId(int position) {
            if (metadata.size() <= position) {
                return 0;
            }
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (metadata.size() <= position) {
                return null;
            }
            View thiz = convertView;
            if (convertView == null) {
                thiz = new TextView(parent.getContext());
                ((TextView) thiz).setTextColor(getResources().getColor(android.R.color.white));
            }
            int viewHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
            if (!isExpanded) {
                viewHeight = parent.getHeight();
            }
            thiz.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, viewHeight));
            ((TextView) thiz).setText(metadata.get(position).toString());
            return thiz;
        }

        @Override
        public int getItemViewType(int position) {
            return 1;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return metadata.isEmpty();
        }
    }

    private class ScrollListener implements OnScrollListener {


        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == SCROLL_STATE_IDLE) {


                MetadataListview.this.post(new Runnable() {
                    @Override
                    public void run() {
                        MetadataListview.this.setIsScrolling(false);
                    }
                });


            } else {
                MetadataListview.this.post(new Runnable() {
                    @Override
                    public void run() {
                        MetadataListview.this.setIsScrolling(true);
                    }
                });
            }

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            MetadataListview.this.post(new Runnable() {
                @Override
                public void run() {
                    MetadataListview.this.setIsScrolling(true);
                }
            });
        }
    }

}
