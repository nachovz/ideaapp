package com.grupoidea.ideaapp.components;

import android.content.Context;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.activities.ParentMenuActivity;

public abstract class ParentBannerProducto extends BaseAdapter {
    /**Actividad padre*/
	protected ParentMenuActivity menuActivity;
	
	public ParentBannerProducto(Context context) {
		menuActivity = (ParentMenuActivity) context;
	}
	
	public void showCarrito() {
		menuActivity.showRightMenu();
	}

    public void hideCarrito() {
        menuActivity.hideMenuRight();
    }
	
	public void setTotalCarrito(String total) {
		TextView textView;
		RelativeLayout layout;
		
		layout = (RelativeLayout) menuActivity.getRightMenuLayout();
		if(layout != null) {
			layout = (RelativeLayout) layout.findViewById(R.id.carrito_total_layout);
			if(layout != null) {
				textView = (TextView) layout.findViewById(R.id.carrito_total_precio_text_view);
				if(textView != null) {
					textView.setText(total);
				}
			}
		}
	}

}
