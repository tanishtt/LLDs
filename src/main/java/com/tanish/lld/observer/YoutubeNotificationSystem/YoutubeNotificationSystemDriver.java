package com.tanish.lld.observer.YoutubeNotificationSystem;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

//DOMAIN
//subscriber or creator.
class User{
    private final String userId;
    private final String name;
    private final String email;
    private final String phone;
    private final String pushToken;


    User(String userId, String name, String email, String phone, String pushToken) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.pushToken = pushToken;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getPushToken() {
        return pushToken;
    }
}

class Video{
    private final String videoId;
    private final String title;
    private final String description;
    private final String channelId;
    private final long uploadedAt;

    Video(String videoId, String title, String description, String channelId) {
        this.videoId = videoId;
        this.title = title;
        this.description = description;
        this.channelId = channelId;
        this.uploadedAt = System.currentTimeMillis();
    }

    public String getVideoId() {
        return videoId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getChannelId() {
        return channelId;
    }

    public long getUploadedAt() {
        return uploadedAt;
    }
}

//notification channel
enum NotificationType{SMS, EMAIL, PUSH}
interface NotificationChannel{
    void send(User user, String message);
}
class EmailNotificationChannel implements NotificationChannel{

    @Override
    public void send(User user, String message) {
        System.out.println( "[EMAIL] To=" + user.getEmail() + " | " + message );
    }
}
class SMSNotificationChannel implements NotificationChannel{
    @Override
    public void send(User user, String message) {
        System.out.println( "[SMS] To=" + user.getPhone() + " | " + message );
    }
}
class PushNotification implements NotificationChannel{

    @Override
    public void send(User user, String message) {
        System.out.println( "[PUSH] User=" + user.getPushToken() + " | " + message );
    }
}

class NotificationFactory{
    public static NotificationChannel create(NotificationType type){
        return switch (type){
            case EMAIL -> new EmailNotificationChannel();
            case SMS -> new SMSNotificationChannel();
            case PUSH -> new PushNotification();
        };
    }
}

//OBSERVER DESIGN PATTERN
//OBSERVER
interface ChannelObserver{
    void updateOnNewVideo(Video video);
}
class Subscription implements ChannelObserver{
    private final User subscriber;
    //String subscriptionId;
    private final List<NotificationChannel> channels;

    Subscription(User user, List<NotificationChannel> channels) {
        this.subscriber = user;
        this.channels = channels;
    }

    public User getSubscriber(){
        return subscriber;
    }

    @Override
    public void updateOnNewVideo(Video video) {
        String message = "New video uploaded: " + video.getTitle() + " | Channel: " + video.getChannelId() + " | Time: " + Instant.ofEpochMilli( video.getUploadedAt());
        for (NotificationChannel channel : channels){
            channel.send(subscriber,message);
        }
    }
}
//SUBJECT
interface ChannelObservable{
    void subscribe(ChannelObserver observer);
    void unsubscribe(ChannelObserver observer);
    void notifySubscriptions(Video video);
}
class YoutubeChannel implements ChannelObservable{
    private final String channelId;
    private final String channelName;
    private final User creator;

    private final List<ChannelObserver> observers=new ArrayList<>();
    private final List<Video> videos = new ArrayList<>();//kind of saving all the videos of that youtube channel
    YoutubeChannel(String channelId, String channelName, User creator) {
        this.channelId = channelId;
        this.channelName = channelName;
        this.creator = creator;
    }

    @Override
    public void subscribe(ChannelObserver observer) {
        observers.add(observer);
    }

    @Override
    public void unsubscribe(ChannelObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifySubscriptions(Video video) {
        for (ChannelObserver observer : observers){
            observer.updateOnNewVideo(video);
        }
    }

    public void uploadVideo(Video video){
        videos.add(video);
        notifySubscriptions(video);
    }

    public User getCreator() {
        return creator;
    }
}

//SERVICE
class YoutubeNotificationService{
    private final Map<String, YoutubeChannel> channels=new ConcurrentHashMap<>();
    private final Map<String, Subscription> subscriptions=new ConcurrentHashMap<>();
    //creator
    public String createChannel(User creator, String channelName){
        String channelId= UUID.randomUUID().toString();
        YoutubeChannel youtubeChannel=new YoutubeChannel(channelId,channelName,creator);
        channels.put(channelId, youtubeChannel);
        System.out.println( "Channel created: " + channelName );
        return channelId;
    }
    public String subscribe(User subscriber, String channelId, List<NotificationType> notificationTypes){
        List<NotificationChannel> notificationChannels=new ArrayList<>();
        for (NotificationType type:notificationTypes){
            notificationChannels.add(NotificationFactory.create(type));
        }
        YoutubeChannel youtubeChannel=channels.get(channelId);
        String subscriptionId = UUID.randomUUID().toString();
        Subscription observer=new Subscription(subscriber, notificationChannels);
        youtubeChannel.subscribe(observer);
        subscriptions.put( subscriptionId, observer );
        return subscriptionId;

    }
    public boolean unsubscribe(String subscriptionId, String channelId){
        YoutubeChannel youtubeChannel=channels.get(channelId);
        Subscription subscription=subscriptions.remove(subscriptionId);
        youtubeChannel.unsubscribe(subscription);
        return true;
    }
    //creator
    public void uploadVideo(User creator, String channelId, String title, String desc){
        YoutubeChannel youtubeChannel=channels.get(channelId);
        if (!youtubeChannel.getCreator() .getUserId() .equals(creator.getUserId())) {
            throw new IllegalArgumentException( "Creator does not own this channel" ); }
        String videoId=UUID.randomUUID().toString();
        Video video=new Video(videoId,title,desc,channelId);
        youtubeChannel.uploadVideo(video);
    }
}

public class YoutubeNotificationSystemDriver {
    public static void main(String[] args) throws InterruptedException {
        YoutubeNotificationService service=new YoutubeNotificationService();
        User creator = new User( "C1", "Tech Creator", "creator@example.com", "+91-9000000001", "creator-device" );
        User alice = new User( "U1", "Alice", "alice@example.com", "+91-9000000002", "alice-device" );
        User bob = new User( "U2", "Bob", "bob@example.com", "+91-9000000003", "bob-device" );
        User charlie = new User( "U3", "Charlie", "charlie@example.com", "+91-9000000004", "charlie-device" );

        String channelId=service.createChannel(creator,"Tech With Me");

        //users subscribing channels
        //alice wants email + push
        String aliceSubscription=service.subscribe(
                alice,
                channelId,
                List.of(NotificationType.EMAIL, NotificationType.PUSH)
        );
        //BOB wants SMS
        String bobSubscription=service.subscribe(
                bob,
                channelId,
                List.of(NotificationType.SMS)
        );
        //charlie wants push
        String charlieSubscription = service.subscribe( charlie, channelId, List.of( NotificationType.PUSH ) );

        //CREATOR UPLOADS VIDEO
        System.out.println( "\n--- VIDEO UPLOAD ---" );
        service.uploadVideo(creator,channelId,"LLD lecture 1","Learn about Observer design pattern in depth");
        Thread.sleep(1000);
        service.uploadVideo(creator,channelId,"LLD lecture 2","Learn about Decorator design pattern in depth");
        Thread.sleep(1000);
        service.unsubscribe(bobSubscription, channelId);
        service.uploadVideo(creator,channelId,"LLD lecture 3","Learn about Facade design pattern in depth");


    }
}
