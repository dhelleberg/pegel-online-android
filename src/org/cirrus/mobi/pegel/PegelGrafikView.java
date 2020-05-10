package org.cirrus.mobi.pegel;

/*	Copyright (C) 2011	Dominik Helleberg

This file is part of pegel-online.

pegel-online is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

pegel-online is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with pegel-online.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class PegelGrafikView extends View{

	private static final int GRADIENT_LENGTH = 50;
	private static final String TAG = "PegelGrafikView";
	private static final int PADDING_TOP = 10;
	private static final int PADDING_LEFT = 5;
	private Paint linePaintBox;
	private float boxheight = 110f;
	private float boxwidth = 30f;
	private float hsw = 0f;
	private float pegel = 0f;
	private float pegelpixel = 0f;
	private float hswpixel = 0f;
	private Paint linePaintHSW;
	private Paint linePaintPegel;
	private Paint gradientBoxPaint;
	private Paint boxBg;
	private Paint miniRectPaint;
	private float scalefactor = 1.0f;
	private List<AdditonalPoint> additionalPoints;
	private Paint linePaintAdditionalPaint;
	private String hsw_string;
	private String pegel_string;
	private RectF rect = new RectF();
	

	public PegelGrafikView(Context context, AttributeSet set) {
		super(context, set);

		linePaintBox = new Paint();
		linePaintBox.setAntiAlias(true);
		linePaintBox.setDither(true);
		linePaintBox.setColor(0xFFFFFFFF);//white
		linePaintBox.setStrokeWidth(1f);
		
		linePaintHSW = new Paint();
		linePaintHSW.setAntiAlias(true);
		linePaintHSW.setDither(true);
		linePaintHSW.setColor(0xFFFF0000);
		linePaintHSW.setStrokeWidth(2f);
		linePaintHSW.setTextSize(12f);
		linePaintHSW.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));		

		linePaintPegel = new Paint();
		linePaintPegel.setAntiAlias(true);
		linePaintPegel.setDither(true);
		linePaintPegel.setColor(0xFF33B9FF);
		linePaintPegel.setStrokeWidth(2f);
		linePaintPegel.setTextSize(12f);
		linePaintPegel.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
		
		gradientBoxPaint = new Paint();
		gradientBoxPaint.setAntiAlias(true);
		gradientBoxPaint.setDither(true);
		gradientBoxPaint.setShader(new LinearGradient(0, 0, 0, GRADIENT_LENGTH, new int[]{Color.RED, Color.YELLOW, Color.GREEN},new float[]{0f,0.5f,1f}, Shader.TileMode.CLAMP));
		gradientBoxPaint.setStrokeWidth(1f);	
		
		boxBg = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
		boxBg.setColor(Color.GREEN);
		
		this.miniRectPaint = new Paint();
		this.miniRectPaint.setColor(0x44000000);
		this.miniRectPaint.setStrokeWidth(1f);

		linePaintAdditionalPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
		linePaintAdditionalPaint.setColor(0xFF888888);
		linePaintAdditionalPaint.setStrokeWidth(2f);
		linePaintAdditionalPaint.setTextSize(12f);
		linePaintAdditionalPaint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));		

		
		this.additionalPoints = new ArrayList<PegelGrafikView.AdditonalPoint>();
		
		this.scalefactor = getContext().getResources().getDisplayMetrics().density;
		Log.v(TAG, "Scale factor:" +scalefactor);

		if(isInEditMode())
		{
			this.setHSW(50f);
			this.setMeasure(25f);
		}
	}

	public void setHSW(float hsw)
	{
		Log.v(TAG, "got hsw: "+hsw);
		this.hsw = hsw;
		this.hsw_string = getResources().getText(R.string.pgview_hsw)+""+hsw;
		//TODO: additional data
		this.calcPixels();
	}

	private void calcPixels() {
		//we need both values
		if(hsw == 0 || pegel == 0)
			return;
		
		//we have everything, make the view visible!
		setVisibility(VISIBLE);
		
		float max = 0f;
		//calculate pixels
		if(hsw > pegel)
		{
			this.hswpixel = 1f;
			this.pegelpixel = 1f+(boxheight - ((boxheight / hsw) * pegel));
			max = hsw;
		}
		else
		{
			this.pegelpixel = 1f;
			this.hswpixel = 1f+(boxheight - ((boxheight / pegel) * hsw));
			max = pegel;
		}
		Log.v(TAG, "calculated: "+hswpixel+" for hsw: "+hsw+" and "+pegelpixel+" for "+pegel);
		
		//calc pixels for additionalpoints
		for (Iterator<AdditonalPoint> i = additionalPoints.iterator(); i.hasNext();) {
			AdditonalPoint ap = i.next();
			ap.pixel = 1f+(boxheight - ((boxheight / max) * ap.value));
		}
		
		invalidate();
	}

	public void setMeasure(float measure)
	{
		Log.v(TAG, "got measure: "+measure);
		this.pegel = measure;
		this.pegel_string = getResources().getText(R.string.pgview_pegel)+""+measure;
		this.calcPixels();
	}
	
	@Override
	public void onSizeChanged(int width, int height, int oldw, int oldh)
	{
		Log.v(TAG, "size-changed: "+width+" "+height);
//		this.width = width;
//		this.height = height;
	}

	//we want the maximum square size 
	@Override
	public void onMeasure (int widthMeasureSpec, int heightMeasureSpec)
	{
		Log.v(TAG, "onMeasure called: wm: "+widthMeasureSpec+" wh: "+heightMeasureSpec);

		super.setMeasuredDimension(Math.round(110*scalefactor),Math.round(boxheight*scalefactor)+PADDING_TOP+10);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		
		//transparent bg
		canvas.drawColor(0x00000000); 
		
		canvas.scale(scalefactor, scalefactor );
		canvas.translate(PADDING_LEFT, PADDING_TOP);
		
		//draw bg
		rect.set(1, 1, boxwidth, boxheight);
		canvas.drawRect(rect, boxBg);
				
		//draw hsw
		if(hsw != 0f)
		{	
			if(pegel < hsw)
			{
				rect.set(1, hswpixel, boxwidth, GRADIENT_LENGTH);
				canvas.drawRect(rect, this.gradientBoxPaint);
			}
			else
			{
				rect.set(1,pegelpixel, boxwidth, hswpixel);
				canvas.drawRect(rect,this.linePaintHSW);
				rect.set(1, hswpixel, boxwidth, hswpixel+GRADIENT_LENGTH);
				canvas.drawRect(rect, this.gradientBoxPaint);
			}
		}

		//draw miniRects
		for(int i = 0; i < (boxheight/8); i++)
		{			
			rect.set(1, 1+(i*8), 12, 1+(i*8)+4);
			canvas.drawRect(rect, miniRectPaint);
		}
		//draw box		
		canvas.drawLine(1, 1, boxwidth,1, linePaintBox);
		canvas.drawLine(1, 1, 1, boxheight, linePaintBox);
		canvas.drawLine(1, boxheight, boxwidth, boxheight, linePaintBox);
		canvas.drawLine(boxwidth, 1, boxwidth, boxheight, linePaintBox);

		
		//draw additional points
		//calc pixels for additionalpoints
		for (Iterator<AdditonalPoint> i = additionalPoints.iterator(); i.hasNext();) {
			AdditonalPoint ap = i.next();
			canvas.drawLine(0, ap.pixel, boxwidth+11, ap.pixel, this.linePaintAdditionalPaint);
			canvas.drawText(ap.name, boxwidth+11, ap.pixel+4, this.linePaintAdditionalPaint);
		}	
		
		//draw hsw
		if(hsw != 0f)
		{	
			canvas.drawLine(0, hswpixel, boxwidth+11, hswpixel, linePaintHSW);
			if(hsw > pegel)
				canvas.drawText(hsw_string, boxwidth+11, hswpixel, linePaintHSW);
			else
				canvas.drawText(hsw_string, boxwidth+11, hswpixel+9, linePaintHSW);
		}

		//draw pegel
		if(pegel != 0f)
		{
			canvas.drawLine(0, pegelpixel, boxwidth+11, pegelpixel, linePaintPegel);
			if(hsw > pegel)
				canvas.drawText(pegel_string, boxwidth+11, pegelpixel+9, linePaintPegel);
			else
				canvas.drawText(pegel_string, boxwidth+11, pegelpixel, linePaintPegel);
		}

	}

	public void addAdditionalData(String name, String value) {
		float val= Float.parseFloat(value);
		this.additionalPoints.add(new AdditonalPoint(name, val, 0f));
		Log.v(TAG, "got additional data: "+name+" "+val);
		this.calcPixels();
	}

	class AdditonalPoint
	{
		public AdditonalPoint(String name, float value, float pixel) {
			super();
			this.name = name;
			this.value = value;
			this.pixel = pixel;
		}
		String name;
		float value;
		float pixel;
		
	}
}
