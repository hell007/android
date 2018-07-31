
## Android中常用设计模式


#### 1、单例模式

概念：动态确保某一个类只有一个实例，而且自行实例化并向整个系统提供这个实例。

优点：  

1.1、由于单例模式在内存中只有一个实例，减少了内存开销。对于那些耗内存的类，只实例化一次，大大提高性能，尤其是移动开发中。

1.2、单例模式可以避免对资源的多重占用，例如一个写文件时，由于只有一个实例存在内存中，避免对同一个资源文件的同时写操作。

1.3、单例模式可以在系统设置全局的访问点，优化和共享资源访问。

```
public class Singleton {  
    private volatile static  Singleton instance = null;  
  
    private Singleton(){  
    }  
   
    public static Singleton getInstance() {  
        if (instance == null) {  
            synchronized (Singleton.class) {  
                if (instance == null) {  
                    instance = new Singleton();  
                }  
            }  
        }  
        return instance;  
    }  
}  

```

构造函数私有化，定义静态函数获得实例就不多说了，这里着重说一下volatile:

volatile本质是在告诉jvm当前变量在寄存器中的值是不确定的,需要从内存中读取,synchronized则是锁定当前变量,只有当前线程可以访问该变量,其他线程被阻塞住.

(首先我们要先意识到有这样的现象,编译器为了加快程序运行的速度,对一些变量的写操作会先在寄存器或者是CPU缓存上进行,最后才写入内存.
而在这个过程,变量的新值对其他线程是不可见的.而volatile的作用就是使它修饰的变量的读写操作都必须在内存中进行!)

synchronized 

同步块大家都比较熟悉，通过 synchronized 关键字来实现，所有加上synchronized 和 块语句，在多线程访问的时候，同一时刻只能有一个线程能够用
synchronized 修饰的方法 或者 代码块。

volatile

用volatile修饰的变量，线程在每次使用变量的时候，都会读取变量修改后的最新的值。volatile很容易被误用，用来进行原子性操作。

再就是这个双重判断null :

这是因为如果线程A进入了该代码，线程B 在等待，这是A线程创建完一个实例出来后，线程B 获得锁进入同步代码，实例已经存在，木有必要再创建一个，
所以双重判断有必要。

Android中 用到的地方很多，比如Android-Universal-Image-Loader中的单例，EventBus中的单例
最后给出一个管理我们activity的类，可以作为一个简单工具类

```

public class ActivityManager {  
  
    private static volatile ActivityManager instance;  
    private Stack<Activity> mActivityStack = new Stack<Activity>();  
      
    private ActivityManager(){  
          
    }  
      
    public static ActivityManager getInstance(){  
        if (instance == null) {  
            synchronized (ActivityManager.class) {  
                if (instance == null) {  
                    instance = new ActivityManager();  
                }  
            }
        }
        return instance;  
    }  
      
    public void addActicity(Activity act){  
        mActivityStack.push(act);  
    }  
      
    public void removeActivity(Activity act){  
        mActivityStack.remove(act);  
    }  
      
    public void killMyProcess(){  
        int nCount = mActivityStack.size();  
        for (int i = nCount - 1; i >= 0; i--) {  
            Activity activity = mActivityStack.get(i);  
            activity.finish();  
        }  
          
        mActivityStack.clear();  
        android.os.Process.killProcess(android.os.Process.myPid());  
    }  
}  

```

##### 单例模式在Android源码中的应用：

在Android源码中，使用到单例模式的例子很多，如：

InputMethodManager类

```

public final class InputMethodManager {
    static final boolean DEBUG = false;
    static final String TAG = "InputMethodManager";
 
    static final Object mInstanceSync = new Object();
    static InputMethodManager mInstance;
    
    final IInputMethodManager mService;
    final Looper mMainLooper;

```

创建唯一的实例static InputMethodManager mInstance;

```
/**
     * Retrieve the global InputMethodManager instance, creating it if it
     * doesn't already exist.
     * @hide
     */
    static public InputMethodManager getInstance(Context context) {
        return getInstance(context.getMainLooper());
    }
    
    /**
     * Internally, the input method manager can't be context-dependent, so
     * we have this here for the places that need it.
     * @hide
     */
    static public InputMethodManager getInstance(Looper mainLooper) {
        synchronized (mInstanceSync) {
            if (mInstance != null) {
                return mInstance;
            }
            IBinder b = ServiceManager.getService(Context.INPUT_METHOD_SERVICE);
            IInputMethodManager service = IInputMethodManager.Stub.asInterface(b);
            mInstance = new InputMethodManager(service, mainLooper);
        }
        return mInstance;
    }
    
```

防止多线程同时创建实例：

```
synchronized (mInstanceSync) {
if (mInstance != null) {
    return mInstance;
}
```

当没有创建实例对象时，调用mInstance = new InputMethodManager(service, mainLooper);

其中类构造函数如下所示：

```
InputMethodManager(IInputMethodManager service, Looper looper) {
    mService = service;
    mMainLooper = looper;
    mH = new H(looper);
    mIInputContext = new ControlledInputConnectionWrapper(looper,
            mDummyInputConnection);

    if (mInstance == null) {
        mInstance = this;
    }
}

```

##### 2、建造者模式（Builder 模式）

定义：将一个复杂对象的构建与它的表示分离，使得同样的构建过程可以创建不同的表示

概念就是比较抽象的，让大家很难理解的，如果简单从这个一个概念就搞懂了这个模式的话，那就不用费力的去查资料整理后边的东西了。
这里我们通过一个例子来引出Build模式。假设有一个Person类，他的一些属性可以为null,可以通过这个类来构架一大批人


```

public class Person {  
    private String name;  
    private int age;  
    private double height;  
    private double weight;  
  
    public String getName() {  
        return name;  
    }  
  
    public void setName(String name) {  
        this.name = name;  
    }  
  
    public int getAge() {  
        return age;  
    }  
  
    public void setAge(int age) {  
        this.age = age;  
    }  
  
    public double getHeight() {  
        return height;  
    }  
  
    public void setHeight(double height) {  
        this.height = height;  
    }  
  
    public double getWeight() {  
        return weight;  
    }  
  
    public void setWeight(double weight) {  
        this.weight = weight;  
    }  
}

```

然后为了方便，你可能会写这么一个构造函数来传属性

```
public Person(String name, int age, double height, double weight) {  
    this.name = name;  
    this.age = age;  
    this.height = height;  
    this.weight = weight;  
}  

```
或者为了更方便还会写一个空的构造函数

```
public Person() {  
}

```

有时候还会比较懒，只传入某些参数，又会来写这些构造函数

```
public Person(String name) {  
    this.name = name;  
}  
  
public Person(String name, int age) {  
    this.name = name;  
    this.age = age;  
}  
  
public Person(String name, int age, double height) {  
    this.name = name;  
    this.age = age;  
    this.height = height;  
}

```

于是就可以来创建各种需要的类

```
Person p1=new Person();  
Person p2=new Person("张三");  
Person p3=new Person("李四",18);  
Person p4=new Person("王二",21,180);  
Person p5=new Person("麻子",16,170,65.4); 

```

其实这种写法的坏处在你写的过程中想摔键盘的时候就该想到了，既然就是一个创建对象的过程，怎么这么繁琐，并且构造函数参数过多，
其他人创建对象的时候怎么知道各个参数代表什么意思呢，

这个时候我们为了代码的可读性，就可以用一下Builder模式了给Person类添加一个静态Builder类，然后修改Person的构造函数，如下：

```
public class Person {  
    private String name;  
    private int age;  
    private double height;  
    private double weight;  
  
    privatePerson(Builder builder) {  
        this.name=builder.name;  
        this.age=builder.age;  
        this.height=builder.height;  
        this.weight=builder.weight;  
    }  
    public String getName() {  
        return name;  
    }  
  
    public void setName(String name) {  
        this.name = name;  
    }  
  
    public int getAge() {  
        return age;  
    }  
  
    public void setAge(int age) {  
        this.age = age;  
    }  
  
    public double getHeight() {  
        return height;  
    }  
  
    public void setHeight(double height) {  
        this.height = height;  
    }  
  
    public double getWeight() {  
        return weight;  
    }  
  
    public void setWeight(double weight) {  
        this.weight = weight;  
    }  
  
    static class Builder{  
        private String name;  
        private int age;  
        private double height;  
        private double weight;  
        public Builder name(String name){  
            this.name=name;  
            return this;  
        }  
        public Builder age(int age){  
            this.age=age;  
            return this;  
        }  
        public Builder height(double height){  
            this.height=height;  
            return this;  
        }  
  
        public Builder weight(double weight){  
            this.weight=weight;  
            return this;  
        }  
  
        public Person build(){  
            return new Person(this);  
        }  
    }  
}

```

从上边代码我们可以看到我们在Builder类中定义了一份跟Person类一样的属性，通过一系列的成员函数进行赋值，但是返回的都是this,
最后提供了一个build函数来创建person对象，对应的在Person的构造函数中，传入了Builder对象，然后依次对自己的成员变量进行赋值。
此外，Builder的成员函数返回的都是this的另一个作用就是让他支持链式调用，使代码可读性大大增强

于是我们就可以这样创建Person对象

```
Person.Builder builder=new Person.Builder();  
Person person=builder  
        .name("张三")  
        .age(18)  
        .height(178.5)  
        .weight(67.4)  
        .build();

```

是不是有那么点感觉了呢

Android中大量地方运用到了Builder模式，比如常见的对话框创建

```

AlertDialog.Builder builder=new AlertDialog.Builder(this);  
AlertDialog dialog=builder.setTitle("对话框")  
        .setIcon(android.R.drawable.ic_dialog)  
        .setView(R.layout.custom_view)  
        .setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {  
            @Override  
            public void onClick(DialogInterface dialog, int which) {  
  
            }  
        })  
        .setNegativeButton(R.string.negative, new DialogInterface.OnClickListener() {  
            @Override  
            public void onClick(DialogInterface dialog, int which) {  
  
            }  
        })  
        .create();  
dialog.show();

```

其实在java中StringBuilder 和StringBuffer都用到了Builder模式，只不过是稍微简单一点了

Gson中的GsonBuilder

```
GsonBuilder builder=new GsonBuilder();  
Gson gson=builder.setPrettyPrinting()  
        .disableHtmlEscaping()  
        .generateNonExecutableJson()  
        .serializeNulls()  
        .create();  

```        

网络框架OKHttp

```
Request.Builder builder=new Request.Builder();  
Request request=builder.addHeader("","")  
    .url("")  
    .post(body)  
    .build(); 
    
```

可见大量框架运用了Builder 设计模式，总结一下吧：

定义一个静态内部类Builder，内部成员变量跟外部一样

Builder通过一系列方法给成员变量赋值，并返回当前对象（this）

Builder类内部提供一个build方法方法或者create方法用于创建对应的外部类，该方法内部调用了外部类的一个私有化构造方法，
该构造方法的参数就是内部类Builder

外部类提供一个私有化的构造方法供内部类调用，在该构造函数中完成成员变量的赋值
    

##### 3、 观察者模式

定义:定义对象间一种一对多的依赖关系，使得当一个对象改变状态，则所有依赖于它的对象都会得到通知并被自动更新。

主要包括四个部分：

```
1. Subject被观察者。是一个接口或者是抽象类，定义被观察者必须实现的职责，它必须能偶动态地增加、取消观察者，管理观察者并通知观察者。
2. Observer观察者。观察者接收到消息后，即进行update更新操作，对接收到的信息进行处理。
3. ConcreteSubject具体的被观察者。定义被观察者自己的业务逻辑，同时定义对哪些事件进行通知。
4. ConcreteObserver具体观察者。每个观察者在接收到信息后处理的方式不同，各个观察者有自己的处理逻辑

```

这个好像还好理解那么一点点，不过还是先来讲个情景，
天气预报的短信服务，一旦付费订阅，每次天气更新都会向你及时发送
其实就是我们无需每时每刻关注我们感兴趣的东西，我们只需要订阅它即可，一旦我们订阅的事务有变化了，被订阅的事务就会即时的通知我们


我们来看一下观察者模式的组成：

```
观察者，我们称它为Observer，有时候我们也称它为订阅者，即Subscriber
被观察者，我们称它为Observable，即可以被观察的东西，有时候还会称之为主题，即Subject
至于观察者模式的具体实现，java里为我们提供了Observable类和Observer接口供我们快速实现该模式，但是这里为了加深印象，不用这个两个类
我们来模拟上边的场景，先定义一个Weather的类

```
```
public class Weather {  
    private String description;  
  
    public Weather(String description) {  
        this.description = description;  
    }  
  
    public String getDescription() {  
        return description;  
    }  
  
    public void setDescription(String description) {  
        this.description = description;  
    }  
  
    @Override  
    public String toString() {  
        return "Weather{" +  
                "description='" + description + '\'' +  
                '}';  
    }  
} 

```

然后定义我们的被观察着，我们希望它能够通用，所以定义成泛型，内部应该暴露出register和unRegister供观察者订阅和取消订阅，
至于观察者的保存，我们用ArrayList即可，另外，当主题发生变化的时候，需要通知观察者来做出响应，还需要一个notifyObservers方法，

具体实现如下

```
public class Observable<T> {  
    List<Observer<T>> mObservers = new ArrayList<Observer<T>>();  
  
    public void register(Observer<T> observer) {  
        if (observer == null) {  
            throw new NullPointerException("observer == null");  
        }  
        synchronized (this) {  
            if (!mObservers.contains(observer))  
                mObservers.add(observer);  
        }  
    }  
  
    public synchronized void unregister(Observer<T> observer) {  
        mObservers.remove(observer);  
    }  
  
    public void notifyObservers(T data) {  
        for (Observer<T> observer : mObservers) {  
            observer.onUpdate(this, data);  
        }  
    }  
  
}

```

而我们的观察者只需要实现一个观察者的接口Observer,该接口也是泛型的

```
public interface Observer<T> {  
    void onUpdate(Observable<T> observable,T data);  
} 
```

一旦订阅的主题发生了变化，就会调用该接口

我们定义一个天气变化的主题，也就是被观察者，再定义两个观察者来观察天气的变化，一旦变化了就打印出天气的情况，
注意，一定要用register方法来注册，否则观察者收不到变化的信息，而一旦不感兴趣，就可以调用unregister方法

```

public class Main {  
    public static void main(String [] args){  
        Observable<Weather> observable=new Observable<Weather>();  
        Observer<Weather> observer1=new Observer<Weather>() {  
            @Override  
            public void onUpdate(Observable<Weather> observable, Weather data) {  
                System.out.println("观察者1："+data.toString());  
            }  
        };  
        Observer<Weather> observer2=new Observer<Weather>() {  
            @Override  
            public void onUpdate(Observable<Weather> observable, Weather data) {  
                System.out.println("观察者2："+data.toString());  
            }  
        };  
  
        observable.register(observer1);  
        observable.register(observer2);  
  
  
        Weather weather=new Weather("晴转多云");  
        observable.notifyObservers(weather);  
  
        Weather weather1=new Weather("多云转阴");  
        observable.notifyObservers(weather1);  
  
        observable.unregister(observer1);  
  
        Weather weather2=new Weather("台风");  
        observable.notifyObservers(weather2);  
  
    }  
}  

```

输出也没有问题

```
观察者1：Weather{description=’晴转多云’}
观察者2：Weather{description=’晴转多云’}
观察者1：Weather{description=’多云转阴’}
观察者2：Weather{description=’多云转阴’}
观察者2：Weather{description=’台风’}
```

好，我们来看一下在Android中的应用，从最简单的开始，Button的点击事件

```
Button btn=new Button(this);  
btn.setOnClickListener(new View.OnClickListener() {  
    @Override  
    public void onClick(View v) {  
        Log.e("TAG","click");  
    }  
});

```

另外广播机制，本质也是观察者模式
调用registerReceiver方法注册广播，调用unregisterReceiver方法取消注册，之后使用sendBroadcast发送广播，
之后注册的广播会受到对应的广播信息，这就是典型的观察者模式


开源框架EventBus也是基于观察者模式的，

观察者模式的注册，取消，发送事件三个典型方法都有

```
EventBus.getDefault().register(Object subscriber);  
EventBus.getDefault().unregister(Object subscriber);  
  
EventBus.getDefault().post(Object event); 

```

##### 4 、策略模式

定义：策略模式定义了一系列算法，并将每一个算法封装起来，而且使他们可以相互替换，策略模式让算法独立于使用的客户而独立改变

最常见的就是关于出行旅游的策略模式，出行方式有很多种，自行车，汽车，飞机，火车等，如果不使用任何模式，代码是这样子的

```
public class TravelStrategy {  
    enum Strategy{  
        WALK,PLANE,SUBWAY  
    }  
    private Strategy strategy;  
    public TravelStrategy(Strategy strategy){  
        this.strategy=strategy;  
    }  
      
    public void travel(){  
        if(strategy==Strategy.WALK){  
            print("walk");  
        }else if(strategy==Strategy.PLANE){  
            print("plane");  
        }else if(strategy==Strategy.SUBWAY){  
            print("subway");  
        }  
    }  
      
    public void print(String str){  
        System.out.println("出行旅游的方式为："+str);  
    }  
      
    public static void main(String[] args) {  
        TravelStrategy walk=new TravelStrategy(Strategy.WALK);  
        walk.travel();  
          
        TravelStrategy plane=new TravelStrategy(Strategy.PLANE);  
        plane.travel();  
          
        TravelStrategy subway=new TravelStrategy(Strategy.SUBWAY);  
        subway.travel();  
    }  
} 

```

很明显，如果需要增加出行方式就需要在增加新的else if语句，这违反了面向对象的原则之一，对修改封装（开放封闭原则）
题外话：面向对象的三大特征：封装，继承和多态

五大基本原则：单一职责原则（接口隔离原则），开放封闭原则，Liskov替换原则，依赖倒置原则，良性依赖原则

好，回归主题，如何用策略模式来解决这个问题

首先，定义一个策略的接口

```
public interface Strategy {  
    void travel();  
} 

```

然后根据不同的出行方法来实现该接口

```
public class WalkStrategy implements Strategy{  
  
    @Override  
    public void travel() {  
        System.out.println("walk");  
    }  
} 

public class PlaneStrategy implements Strategy{  
  
    @Override  
    public void travel() {  
        System.out.println("plane");  
    }  
} 


public class SubwayStrategy implements Strategy{  
  
    @Override  
    public void travel() {  
        System.out.println("subway");  
    }  
}  

```

此外还需要一个包装策略的类，来调用策略中的接口

```

public class TravelContext {  
    Strategy strategy;  
  
    public Strategy getStrategy() {  
        return strategy;  
    }  
  
    public void setStrategy(Strategy strategy) {  
        this.strategy = strategy;  
    }  
  
    public void travel() {  
        if (strategy != null) {  
            strategy.travel();  
        }  
    }  
} 

```

测试一下代码

```
public class Main {  
    public static void main(String[] args) {  
        TravelContext travelContext=new TravelContext();  
        travelContext.setStrategy(new PlaneStrategy());  
        travelContext.travel();  
        travelContext.setStrategy(new WalkStrategy());  
        travelContext.travel();  
        travelContext.setStrategy(new SubwayStrategy());  
        travelContext.travel();  
    }  
} 

```

以后如果再增加什么别的出行方式，就再继承策略接口即可，完全不需要修改现有的类

策略模式优缺点

定义一系列算法：策略模式的功能就是定义一系列算法，实现让这些算法可以相互替换。所以会为这一系列算法定义公共的接口，
以约束一系列算法要实现的功能。如果这一系列算法具有公共功能，可以把策略接口实现成为抽象类，把这些公共功能实现到父类里面，
对于这个问题，前面讲了三种处理方法，这里就不罗嗦了。

避免多重条件语句：根据前面的示例会发现，策略模式的一系列策略算法是平等的，可以互换的，写在一起就是通过if-else结构来组织，
如果此时具体的算法实现里面又有条件语句，就构成了多重条件语句，使用策略模式能避免这样的多重条件语句。

更好的扩展性：在策略模式中扩展新的策略实现非常容易，只要增加新的策略实现类，然后在选择使用策略的地方选择使用这个新的策略实现就好了。

客户必须了解每种策略的不同：策略模式也有缺点，比如让客户端来选择具体使用哪一个策略，这就可能会让客户需要了解所有的策略，
还要了解各种策略的功能和不同，这样才能做出正确的选择，而且这样也暴露了策略的具体实现。

增加了对象数目：由于策略模式把每个具体的策略实现都单独封装成为类，如果备选的策略很多的话，那么对象的数目就会很可观。

只适合扁平的算法结构：策略模式的一系列算法地位是平等的，是可以相互替换的，事实上构成了一个扁平的算法结构，也就是在一个策略接口下，
有多个平等的策略算法，就相当于兄弟算法。而且在运行时刻只有一个算法被使用，这就限制了算法使用的层级，使用的时候不能嵌套使用。

Android中的应用

下面说说在Android里面的应用。在Android里面策略模式的其中一个典型应用就是Adapter，在我们平时使用的时候，一般情况下我们可能继承BaseAdapter，
然后实现不同的View返回，GetView里面实现不同的算法。外部使用的时候也可以根据不同的数据源，切换不同的Adapter。


##### 5、原型模式

定义：用原型实例指定创建对象的种类，并通过拷贝这些原型创建新的对象。

```

public class Person{  
    private String name;  
    private int age;  
    private double height;  
    private double weight;  
  
    public Person(){  
          
    }  
  
    public String getName() {  
        return name;  
    }  
  
    public void setName(String name) {  
        this.name = name;  
    }  
  
    public int getAge() {  
        return age;  
    }  
  
    public void setAge(int age) {  
        this.age = age;  
    }  
  
    public double getHeight() {  
        return height;  
    }  
  
    public void setHeight(double height) {  
        this.height = height;  
    }  
  
    public double getWeight() {  
        return weight;  
    }  
  
    public void setWeight(double weight) {  
        this.weight = weight;  
    }  
  
    @Override  
    public String toString() {  
        return "Person{" +  
                "name='" + name + '\'' +  
                ", age=" + age +  
                ", height=" + height +  
                ", weight=" + weight +  
                '}';  
    }  
} 

```

要实现原型模式，按照以下步骤来：

1，实现一个Cloneable接口

```
public class Person implements Cloneable{  
  
} 

```

重写Object的clone方法，在此方法中实现拷贝逻辑

```

@Override  
public Object clone(){  
    Person person=null;  
    try {  
        person=(Person)super.clone();  
        person.name=this.name;  
        person.weight=this.weight;  
        person.height=this.height;  
        person.age=this.age;  
    } catch (CloneNotSupportedException e) {  
        e.printStackTrace();  
    }  
    return person;  
} 

```

测试一下

```

public class Main {  
    public static void main(String [] args){  
        Person p=new Person();  
        p.setAge(18);  
        p.setName("张三");  
        p.setHeight(178);  
        p.setWeight(65);  
        System.out.println(p);  
  
        Person p1= (Person) p.clone();  
        System.out.println(p1);  
  
        p1.setName("李四");  
        System.out.println(p);  
        System.out.println(p1);  
    }  
} 

```

```
输出结果如下：
Person{name=’张三’, age=18, height=178.0, weight=65.0}
Person{name=’张三’, age=18, height=178.0, weight=65.0}
Person{name=’张三’, age=18, height=178.0, weight=65.0}
Person{name=’李四’, age=18, height=178.0, weight=65.0}
```

试想一下，两个不同的人，除了姓名不一样，其他三个属性都一样，用原型模式进行拷贝就会显得异常简单，这也是原型模式的应用场景之一

假设Person类还有一个属性叫兴趣集合，是一个List集合，就酱紫：

```

private ArrayList<String> hobbies=new ArrayList<String>();  
  
public ArrayList<String> getHobbies() {  
    return hobbies;  
}  
  
public void setHobbies(ArrayList<String> hobbies) {  
    this.hobbies = hobbies;  
} 
```

在进行拷贝的时候就要注意了，如果还是跟之前的一样操作，就会发现其实两个不同的人的兴趣集合的是指向同一个引用，
我们对其中一个人的这个集合属性进行操作 ，另一个人的这个属性也会相应的变化，其实导致这个问题的本质原因是我们只进行了浅拷贝，
也就是指拷贝了引用，最终两个对象指向的引用是同一个，一个发生变化，另一个也会发生拜变化。显然解决方法就是使用深拷贝

```

@Override  
public Object clone(){  
    Person person=null;  
    try {  
        person=(Person)super.clone();  
        person.name=this.name;  
        person.weight=this.weight;  
        person.height=this.height;  
        person.age=this.age;  
  
        person.hobbies=(ArrayList<String>)this.hobbies.clone();  
    } catch (CloneNotSupportedException e) {  
        e.printStackTrace();  
    }  
    return person;  
}  
```

不再是直接引用，而是拷贝了一份，
其实有的时候我们看到的原型模式更多的是另一种写法：在clone函数里调用构造函数，构造函数里传入的参数是该类对象，然后在函数中完成逻辑拷贝


```

@Override  
public Object clone(){  
    return new Person(this);  
}  


public Person(Person person){  
    this.name=person.name;  
    this.weight=person.weight;  
    this.height=person.height;  
    this.age=person.age;  
    this.hobbies= new ArrayList<String>(hobbies);  
}

其实都差不多，只是写法不一样而已
现在 来看看Android中的原型模式：

先看Bundle类，

```
    public Object clone() {  
        return new Bundle(this);  
    }   
    public Bundle(Bundle b) {  
        super(b);  

        mHasFds = b.mHasFds;  
        mFdsKnown = b.mFdsKnown;  
    } 

```

然后是Intent类

```
    @Override  
    public Object clone() {  
        return new Intent(this);  
    }  

    public Intent(Intent o) {  
        this.mAction = o.mAction;  
        this.mData = o.mData;  
        this.mType = o.mType;  
        this.mPackage = o.mPackage;  
        this.mComponent = o.mComponent;  
        this.mFlags = o.mFlags;  
        this.mContentUserHint = o.mContentUserHint;  
        if (o.mCategories != null) {  
            this.mCategories = new ArraySet<String>(o.mCategories);  
        }  
        if (o.mExtras != null) {  
            this.mExtras = new Bundle(o.mExtras);  
        }  
        if (o.mSourceBounds != null) {  
            this.mSourceBounds = new Rect(o.mSourceBounds);  
        }  
        if (o.mSelector != null) {  
            this.mSelector = new Intent(o.mSelector);  
        }  
        if (o.mClipData != null) {  
            this.mClipData = new ClipData(o.mClipData);  
        }  
    }  

```

用法也十分简单，一旦我们要用的Intent与现在的一个Intent很多东西都一样，那我们就可以直接拷贝现有的Intent，
再修改不同的地方，便可以直接使用
    
```
    Uri uri = Uri.parse("smsto:10086");      
    Intent shareIntent = new Intent(Intent.ACTION_SENDTO, uri);      
    shareIntent.putExtra("sms_body", "hello");      

    Intent intent = (Intent)shareIntent.clone() ;  
    startActivity(intent);  

```

网络请求中最常用的OkHttp中，也应用了原型模式,就在OkHttpClient类中，他实现了Cloneable接口
    
```

    @Override   
    public OkHttpClient clone() {  
        return new OkHttpClient(this);  
    }  
    private OkHttpClient(OkHttpClient okHttpClient) {  
        this.routeDatabase = okHttpClient.routeDatabase;  
        this.dispatcher = okHttpClient.dispatcher;  
        this.proxy = okHttpClient.proxy;  
        this.protocols = okHttpClient.protocols;  
        this.connectionSpecs = okHttpClient.connectionSpecs;  
        this.interceptors.addAll(okHttpClient.interceptors);  
        this.networkInterceptors.addAll(okHttpClient.networkInterceptors);  
        this.proxySelector = okHttpClient.proxySelector;  
        this.cookieHandler = okHttpClient.cookieHandler;  
        this.cache = okHttpClient.cache;  
        this.internalCache = cache != null ? cache.internalCache : okHttpClient.internalCache;  
        this.socketFactory = okHttpClient.socketFactory;  
        this.sslSocketFactory = okHttpClient.sslSocketFactory;  
        this.hostnameVerifier = okHttpClient.hostnameVerifier;  
        this.certificatePinner = okHttpClient.certificatePinner;  
        this.authenticator = okHttpClient.authenticator;  
        this.connectionPool = okHttpClient.connectionPool;  
        this.network = okHttpClient.network;  
        this.followSslRedirects = okHttpClient.followSslRedirects;  
        this.followRedirects = okHttpClient.followRedirects;  
        this.retryOnConnectionFailure = okHttpClient.retryOnConnectionFailure;  
        this.connectTimeout = okHttpClient.connectTimeout;  
        this.readTimeout = okHttpClient.readTimeout;  
        this.writeTimeout = okHttpClient.writeTimeout;  
    }  
    
```












