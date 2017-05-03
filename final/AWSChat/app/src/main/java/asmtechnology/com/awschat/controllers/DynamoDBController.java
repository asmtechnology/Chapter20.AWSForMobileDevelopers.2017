package asmtechnology.com.awschat.controllers;

import android.content.Context;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import asmtechnology.com.awschat.models.Friend;
import asmtechnology.com.awschat.models.User;
import asmtechnology.com.awschat.interfaces.DynamoDBControllerGenericHandler;
import asmtechnology.com.awschat.interfaces.DynamoDBControllerRetrieveFriendIDsHandler;
import asmtechnology.com.awschat.interfaces.DynamoDBControllerRetrieveUserHandler;

public class DynamoDBController {

    private Context mContext;

    private static DynamoDBController instance = null;
    private DynamoDBController() {}

    public static DynamoDBController getInstance(Context context) {
        if(instance == null) {
            instance = new DynamoDBController();
        }

        instance.mContext = context;
        return instance;
    }

    public void refreshFriendList(final String userId, final DynamoDBControllerGenericHandler completion) {

        Runnable runnable = new Runnable() {
            public void run() {

                retrieveFriendIds(userId, new DynamoDBControllerRetrieveFriendIDsHandler() {
                    @Override
                    public void didSucceed(ArrayList<String> results) {

                        CognitoIdentityPoolController identityPoolController = CognitoIdentityPoolController.getInstance(mContext);
                        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(identityPoolController.mCredentialsProvider);
                        DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

                        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
                        try {
                            PaginatedScanList<User> users = mapper.scan(User.class, scanExpression);

                            // clear friend list in ChatManager
                            ChatManager chatManager = ChatManager.getInstance(mContext);
                            chatManager.clearFriendList();

                            // add User objects.
                            for (User u : users) {

                                if (results.contains(u.getId())) {
                                    chatManager.addFriend(u);
                                }
                            }

                            completion.didSucceed();

                        } catch (AmazonServiceException ex) {
                            completion.didFail(ex);
                        }
                    }

                    @Override
                    public void didFail(Exception exception) {
                        completion.didFail(exception);
                    }
                });

            }
        };

        Thread mythread = new Thread(runnable);
        mythread.start();
    }

    public void retrieveUser (final String userId, final DynamoDBControllerRetrieveUserHandler completion) {

        Runnable runnable = new Runnable() {

            public void run() {
                CognitoIdentityPoolController identityPoolController = CognitoIdentityPoolController.getInstance(mContext);
                AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(identityPoolController.mCredentialsProvider);
                DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

                try {
                    User user = mapper.load(User.class, userId);

                    completion.didSucceed(user);

                } catch (AmazonServiceException ex) {
                    completion.didFail(ex);
                }
            }
        };

        Thread mythread = new Thread(runnable);
        mythread.start();
    }


    public void refreshPotentialFriendList(final String currentUserId, final DynamoDBControllerGenericHandler completion) {

        Runnable runnable = new Runnable() {

            public void run() {

                retrieveFriendIds(currentUserId, new DynamoDBControllerRetrieveFriendIDsHandler() {
                    @Override
                    public void didSucceed(ArrayList<String> results) {
                        CognitoIdentityPoolController identityPoolController = CognitoIdentityPoolController.getInstance(mContext);
                        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(identityPoolController.mCredentialsProvider);
                        DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

                        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
                        try {
                            PaginatedScanList<User> users = mapper.scan(User.class, scanExpression);

                            // clear potential friend list in ChatManager
                            ChatManager chatManager = ChatManager.getInstance(mContext);
                            chatManager.clearPotentialFriendList();

                            // add users who are not friends.
                            for (User u : users) {

                                if (results.contains(u.getId())) {
                                    continue;
                                }

                                if (u.getId().equals(currentUserId)) {
                                    continue;
                                }

                                chatManager.addPotentialFriend(u);
                            }

                            completion.didSucceed();

                        } catch (AmazonServiceException ex) {
                            completion.didFail(ex);
                        }
                    }

                    @Override
                    public void didFail(Exception exception) {
                        completion.didFail(exception);
                    }
                });
            }
        };

        Thread mythread = new Thread(runnable);
        mythread.start();

    }

    public void addFriend(final String currentUserId,
                   final String friendUserId,
                   final DynamoDBControllerGenericHandler completion) {

        Runnable runnable = new Runnable() {
            public void run() {

                try {
                    CognitoIdentityPoolController identityPoolController = CognitoIdentityPoolController.getInstance(mContext);

                    AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(identityPoolController.mCredentialsProvider);
                    DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

                    Friend friendRelationship = new Friend();
                    friendRelationship.setId(generateUUID());
                    friendRelationship.setUser_id(currentUserId);
                    friendRelationship.setFriend_id(friendUserId);

                    mapper.save(friendRelationship);
                    completion.didSucceed();
                } catch (AmazonServiceException ex) {
                    completion.didFail(ex);
                }
            }
        };
        Thread mythread = new Thread(runnable);
        mythread.start();
    }

    private String generateUUID() {
        UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString();
        return uuidString.toUpperCase();
    }

    private void retrieveFriendIds(String userId, DynamoDBControllerRetrieveFriendIDsHandler completion) {

        CognitoIdentityPoolController identityPoolController = CognitoIdentityPoolController.getInstance(mContext);
        AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(identityPoolController.mCredentialsProvider);
        DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

        Map<String, AttributeValue> attributeValues = new HashMap<String, AttributeValue>();
        attributeValues.put(":val1", new AttributeValue().withS(userId));

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("user_id = :val1")
                .withExpressionAttributeValues(attributeValues);

        try {
            PaginatedScanList<Friend> results = mapper.scan(Friend.class, scanExpression);

            ArrayList<String> friendUserIdList = new ArrayList<String>();

            for (Friend f : results) {
                friendUserIdList.add(f.getFriend_id());
            }

            completion.didSucceed(friendUserIdList);

        } catch (AmazonServiceException ex) {
           completion.didFail(ex);
        }

    }


}
