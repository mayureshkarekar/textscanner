package com.getext.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.getext.R;
import com.getext.keys.AppKeys;
import com.getext.room.entity.RecognizedText;
import com.getext.utils.AppUtils;
import com.google.android.gms.ads.AdView;

public class RecognizedTextAdapter extends ListAdapter<Object, RecyclerView.ViewHolder> {
    private final int ITEM_TYPE_RECOGNIZED_TEXT = 1;
    private final int ITEM_TYPE_BANNER_AD = 2;
    private final LayoutInflater mLayoutInflater;
    private MenuItemClickListener mMenuItemClickListener;

    public interface MenuItemClickListener {
        void onMenuItemClickListener(@IdRes int itemId, @Nullable Object object);
    }

    private static final DiffUtil.ItemCallback<Object> DIFF_CALLBACK = new DiffUtil.ItemCallback<Object>() {
        @Override
        public boolean areItemsTheSame(@NonNull Object oldItem, @NonNull Object newItem) {
            if (oldItem instanceof RecognizedText && newItem instanceof RecognizedText) {
                return ((RecognizedText) oldItem).getId() == ((RecognizedText) newItem).getId();
            }

            return true;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Object oldItem, @NonNull Object newItem) {
            if (oldItem instanceof RecognizedText && newItem instanceof RecognizedText) {
                return ((RecognizedText) oldItem).getRecognizedText().equals(((RecognizedText) newItem).getRecognizedText());
            }

            return true;
        }
    };

    class RecognizedTextViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {
        private final TextView tvLabel;
        private final TextView tvText;
        private final TextView tvMode;
        private final TextView tvDate;

        private RecognizedTextViewHolder(@NonNull View itemView) {
            super(itemView);

            tvLabel = itemView.findViewById(R.id.tv_label);
            tvText = itemView.findViewById(R.id.tv_text);
            tvMode = itemView.findViewById(R.id.tv_mode);
            tvDate = itemView.findViewById(R.id.tv_date);

            itemView.findViewById(R.id.iv_options).setOnClickListener(this);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.iv_options) {
                showPopupMenus(view);
            } else {
                int adapterPosition = getAdapterPosition();
                if (mMenuItemClickListener != null && adapterPosition != RecyclerView.NO_POSITION) {
                    mMenuItemClickListener.onMenuItemClickListener(R.id.menu_edit, getItem(adapterPosition));
                }
            }
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_copy:
                case R.id.menu_edit:
                case R.id.menu_delete: {
                    int adapterPosition = getAdapterPosition();
                    if (mMenuItemClickListener != null && adapterPosition != RecyclerView.NO_POSITION) {
                        mMenuItemClickListener.onMenuItemClickListener(item.getItemId(), getItem(adapterPosition));
                    }
                }
                return true;

                case R.id.menu_delete_all: {
                    if (mMenuItemClickListener != null) {
                        mMenuItemClickListener.onMenuItemClickListener(R.id.menu_delete_all, null);
                    }
                }
                return true;
            }

            return false;
        }

        private void showPopupMenus(View view) {
            PopupMenu options = new PopupMenu(view.getContext(), view);
            options.inflate(R.menu.menu_recognized_text_item);
            options.setOnMenuItemClickListener(this);
            options.show();
        }
    }

    class BannerAdViewHolder extends RecyclerView.ViewHolder {
        private BannerAdViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public RecognizedTextAdapter(Context context) {
        super(DIFF_CALLBACK);
        mLayoutInflater = LayoutInflater.from(context);
    }

    public void setMenuItemClickListener(MenuItemClickListener listener) {
        this.mMenuItemClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return ITEM_TYPE_RECOGNIZED_TEXT;
        } else {
            return (getItem(position) instanceof AdView) ? ITEM_TYPE_BANNER_AD : ITEM_TYPE_RECOGNIZED_TEXT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_BANNER_AD) {
            View bannerLayoutView = mLayoutInflater.inflate(R.layout.layout_banner_ad_list_item, parent, false);
            return new BannerAdViewHolder(bannerLayoutView);
        } else {
            View itemView = mLayoutInflater.inflate(R.layout.layout_recognized_text_list_item, parent, false);
            return new RecognizedTextViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == ITEM_TYPE_BANNER_AD) {
            if (getItem(position) instanceof AdView) {
                BannerAdViewHolder bannerAdViewHolder = (BannerAdViewHolder) holder;
                AdView adView = (AdView) getItem(position);
                ViewGroup adCardView = (ViewGroup) bannerAdViewHolder.itemView;
                if (adCardView.getChildCount() > 0) {
                    adCardView.removeAllViews();
                }
                if (adView.getParent() != null) {
                    ((ViewGroup) adView.getParent()).removeView(adView);
                }

                adCardView.addView(adView);
            }
        } else {
            if (getItem(position) instanceof RecognizedText) {
                RecognizedText currentRecognizedText = (RecognizedText) getItem(position);
                RecognizedTextViewHolder recognizedTextViewHolder = (RecognizedTextViewHolder) holder;

                String recognizedText = currentRecognizedText.getRecognizedText();
                String label = recognizedText.replaceAll("[^a-zA-Z0-9]", "");
                int recognitionMode = currentRecognizedText.getRecognitionMode();
                int recognitionModeString = (recognitionMode == AppKeys.RECOGNITION_MODE_CAMERA) ? R.string.camera : R.string.image;
                int recognitionModeDrawable = (recognitionMode == AppKeys.RECOGNITION_MODE_CAMERA) ? R.drawable.ic_camera_16dp : R.drawable.ic_image_16dp;
                String date = AppUtils.getDateFromTimestamp(currentRecognizedText.getTimestamp());

                recognizedTextViewHolder.tvLabel.setText(label);
                recognizedTextViewHolder.tvText.setText(recognizedText);
                recognizedTextViewHolder.tvMode.setText(recognitionModeString);
                recognizedTextViewHolder.tvMode.setCompoundDrawablesWithIntrinsicBounds(recognitionModeDrawable, 0, 0, 0);
                recognizedTextViewHolder.tvDate.setText(date);
            }
        }
    }
}