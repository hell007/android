
## Android-PickerView 城市三级选择使用


#### 1. viewModle 使用

```

private ArrayList<JsonBean> options1Items = new ArrayList<>();
private ArrayList<ArrayList<String>> options2Items = new ArrayList<>();
private ArrayList<ArrayList<ArrayList<String>>> options3Items = new ArrayList<>();


//解析数据
private void initCityData() {

	//获取assets目录下的json文件数据
	String JsonData = CityUtils.getJson(content, "city.json");
	//用Gson 转成实体
	ArrayList<CityEntity> json = parseData(JsonData);

	//添加省份数据
	options1Items = json;
	//遍历省份
	for (int i = 0; i < json.size(); i++) {
	    //该省的城市列表（第二级）
	    ArrayList<String> CityList = new ArrayList<>();
	    //该省的所有地区列表（第三极）
	    ArrayList<ArrayList<String>> Province_AreaList = new ArrayList<>();
	    //遍历该省份的所有城市
	    for (int c = 0; c < json.get(i).getCityList().size(); c++) {
		String CityName = json.get(i).getCityList().get(c).getName();
		//添加城市
		CityList.add(CityName);
		//该城市的所有地区列表
		ArrayList<String> City_AreaList = new ArrayList<>();

		//如果无地区数据，建议添加空字符串，防止数据为null 导致三个选项长度不匹配造成崩溃
		if (json.get(i).getCityList().get(c).getArea() == null
			|| json.get(i).getCityList().get(c).getArea().size() == 0) {
		    City_AreaList.add("");
		} else {
		    City_AreaList.addAll(json.get(i).getCityList().get(c).getArea());
		}
		//添加该省所有地区数据
		Province_AreaList.add(City_AreaList);
	    }

	    //添加城市数据
	    options2Items.add(CityList);

	    //添加地区数据
	    options3Items.add(Province_AreaList);
	}
}


// 弹出选择器
private void showPickerView() {

	OptionsPickerView pvOptions = new OptionsPickerBuilder(this, new OnOptionsSelectListener() {
	    @Override
	    public void onOptionsSelect(int options1, int options2, int options3, View v) {
		//返回的分别是三个级别的选中位置
		String province = options1Items.get(options1).getPickerViewText();
		String city = options2Items.get(options1).get(options2);
		String count = options3Items.get(options1).get(options2).get(options3);
		String tx = province.equal(city) ? province+count : province+city+count;
		ToastUtils.showshort(tx);
	    }
	})
		.setTitleText("城市选择")
		.setDividerColor(Color.BLACK)
		.setTextColorCenter(Color.BLACK) //设置选中项文字颜色
		.setContentTextSize(20)
		.build();

	/*pvOptions.setPicker(options1Items);//一级选择器
	pvOptions.setPicker(options1Items, options2Items);//二级选择器*/
	pvOptions.setPicker(options1Items, options2Items, options3Items);//三级选择器
	pvOptions.show();
}

```


####  2. cityEntity 

```

import com.contrarywind.interfaces.IPickerViewData;

import java.util.List;

public class JsonBean implements IPickerViewData {


    /**
     * name : 省份
     * city : [{"name":"北京市","area":["东城区","西城区","崇文区","宣武区","朝阳区"]}]
     */

    private String name;
    private List<CityBean> city;

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public List<CityBean> getCityList() {
	return city;
    }

    public void setCityList(List<CityBean> city) {
	this.city = city;
    }

    // 实现 IPickerViewData 接口，
    // 这个用来显示在PickerView上面的字符串，
    // PickerView会通过IPickerViewData获取getPickerViewText方法显示出来。
    @Override
    public String getPickerViewText() {
	return this.name;
    }


    public static class CityBean {
	/**
	 * name : 城市
	 * area : ["东城区","西城区","崇文区","昌平区"]
	 */

	private String name;
	private List<String> area;

	public String getName() {
	    return name;
	}

	public void setName(String name) {
	    this.name = name;
	}

	public List<String> getArea() {
	    return area;
	}

	public void setArea(List<String> area) {
	    this.area = area;
	}
    }
}

```



#### 3. uitls

```

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CityUtils {

    public static String getJson(Context context, String fileName) {

	StringBuilder stringBuilder = new StringBuilder();
	try {
	    AssetManager assetManager = context.getAssets();
	    BufferedReader bf = new BufferedReader(new InputStreamReader(
		    assetManager.open(fileName)));
	    String line;
	    while ((line = bf.readLine()) != null) {
		stringBuilder.append(line);
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return stringBuilder.toString();
    }

    //Gson 解析
    public static ArrayList<CityEntity> parseData(String result) {
	ArrayList<CityEntity> detail = new ArrayList<>();
	try {
	    JSONArray data = new JSONArray(result);
	    Gson gson = new Gson();
	    for (int i = 0; i < data.length(); i++) {
		JsonBean entity = gson.fromJson(data.optJSONObject(i).toString(), JsonBean.class);
		detail.add(entity);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return detail;
    }

}


```

####  4. city.json

后台操作表返回的json数据格式

建议生成个 *.json 的本地文件,降低对后天服务的请求


## 总结


缺点： 假如省市县区表内存储的字段需要是id,那么不符合条件，需要更改源文件





