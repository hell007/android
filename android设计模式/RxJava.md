
## RxJava中的doOnSubscribe默认执行线程分析


**默认情况下， doOnSubscribe() 执行在 subscribe() 发生的线程；
而如果在 doOnSubscribe() 之后有 subscribeOn() 的话，它将执行在离它最近的 subscribeOn() 所指定的线程**

```

Observable.create(onSubscribe)
    .subscribeOn(Schedulers.io())
    .doOnSubscribe(new Action0() {
        @Override
        public void call() {
            progressBar.setVisibility(View.VISIBLE); // 需要在主线程执行
        }
    })
    .subscribeOn(AndroidSchedulers.mainThread()) // 指定主线程
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(subscriber);
    
```

subscribeOn 作用于该操作符之前的 Observable 的创建操符作以及 doOnSubscribe 操作符 ，
换句话说就是 doOnSubscribe 以及 Observable 的创建操作符总是被其之后最近的 subscribeOn 控制

```
Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                subscriber.onNext(1);
                subscriber.onCompleted();
            }
        })
        .doOnSubscribe(new Action0() {

            @Override
            public void call() {
                System.out.println("00doOnSubscribe在线程" + Thread.currentThread().getName() + "中");
            }
        })
        .subscribeOn(Schedulers.newThread())
        .map(new Func1<Integer, String>() {
            @Override
            public String call(Integer integer) {
                System.out.println("map1在线程" + Thread.currentThread().getName() + "中");
                return integer + "";
            }
        })
        .doOnSubscribe(new Action0() {

            @Override
            public void call() {
                System.out.println("11doOnSubscribe在线程" + Thread.currentThread().getName() + "中");
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.io())
        .map(new Func1<String, String>() {
            @Override
            public String call(String s) {
                System.out.println("map2在线程" + Thread.currentThread().getName() + "中");

                return s + "1";
            }
        })
        .doOnSubscribe(new Action0() {

            @Override
            public void call() {
                System.out.println("22doOnSubscribe在线程" + Thread.currentThread().getName() + "中");
            }
        })
        .subscribeOn(Schedulers.newThread())
        .subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                System.out.println("onNext在线程" + Thread.currentThread().getName() + "中");
            }
        });

```

执行结果如下：

```
22doOnSubscribe在线程RxNewThreadScheduler-1中
11doOnSubscribe在线程RxIoScheduler-3中
00doOnSubscribe在线程RxNewThreadScheduler-2中
map1在线程RxNewThreadScheduler-2中
map2在线程RxIoScheduler-2中
onNext在线程RxIoScheduler-2中

```

#### 总结

1. doOnSubscribe()与onStart()类似，均在代码调用时就会回调，但doOnSubscribe()可以通过subscribeOn()操作符改变运行的线程且越在后面运行越早；

2. doOnSubscribe()后面紧跟subscribeOn()，那么doOnSubscribe()将于subscribeOn()指定的线程保持一致；
如果doOnSubscribe()在subscribeOn()之后，他的执行线程得再看情况分析；

3. doOnSubscribe()如果在observeOn()后（注意：observeon()后没有紧接着再调用subcribeon()方法），
那么doOnSubscribe的执行线程就是main线程，与observeon()指定的线程没有关系；

4. 如果在observeOn()之前没有调用过subcribeOn()方法，observeOn()之后subscribe面()方法之前调用subcribeOn()方法，
那么他会改变整个代码流程中所有调用doOnSubscribe()方法所在的线程，同时也会改变observeOn()方法之前所有操作符所在的线程
（有个重要前提：不满足第2点的条件，也就是doOnSubscribe()后面没有调用subscribeOn()方法）；

5. 如果在observeOn()前后都没有调用过subcribeOn()方法，那么整个代码流程中的doOnSubscribe()执行在main线程，与observeOn()指定的线程无关；
同时observeOn()之前的操作符也将执行在main线程，observeOn()之后的操作符与observeOn()指定的线程保持一致。






