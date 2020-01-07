package com.android.dimens.constants;


public enum DimenTypes {

    //适配Android 3.2以上   大部分手机的sw值集中在  300-460之间
	 DP_sw__300(300),  // values-sw300
	 DP_sw__310(310),
	 DP_sw__320(320),  //240x320   320x480  480x800
	 DP_sw__330(330),
	 DP_sw__360(360), //720x1280  1080x1920
	 DP_sw__410(410); //1440x2560
	// 想生成多少自己以此类推
  

    /**
     * 屏幕最小宽度
     */
    private int swWidthDp;




    DimenTypes(int swWidthDp) {

        this.swWidthDp = swWidthDp;
    }

    public int getSwWidthDp() {
        return swWidthDp;
    }

    public void setSwWidthDp(int swWidthDp) {
        this.swWidthDp = swWidthDp;
    }

}
