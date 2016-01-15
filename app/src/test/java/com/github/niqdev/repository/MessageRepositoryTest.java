package com.github.niqdev.repository;

import com.github.niqdev.BuildConfig;
import com.github.niqdev.component.ApplicationComponentTest;
import com.github.niqdev.component.DaggerApplicationComponentTest;
import com.github.niqdev.component.Injector;
import com.github.niqdev.component.module.ApplicationContextModuleTest;
import com.github.niqdev.component.module.RepositoryModuleTest;
import com.github.niqdev.model.MessageModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import io.realm.Realm;
import rx.observers.TestSubscriber;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
@PowerMockIgnore({"org.mockito.*"})
@PrepareForTest({Injector.class, Realm.class})
public class MessageRepositoryTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule();

    @Inject
    MessageRepository messageRepository;

    @Inject
    DatabaseHelper databaseHelper;

    @Before
    public void setupDagger() {
        ApplicationComponentTest applicationComponentTest = DaggerApplicationComponentTest.builder()
            .applicationContextModuleTest(new ApplicationContextModuleTest())
            .repositoryModuleTest(new RepositoryModuleTest(false))
            .build();

        PowerMockito.mockStatic(Injector.class);
        PowerMockito.when(Injector.getApplicationComponent()).thenReturn(applicationComponentTest);

        ((ApplicationComponentTest) Injector.getApplicationComponent()).inject(this);
    }

    @Before
    public void setupRealm() {
        Realm realmMock = PowerMockito.mock(Realm.class);
        PowerMockito.mockStatic(Realm.class);

        when(Realm.getDefaultInstance()).thenReturn(realmMock);
        doNothing().when(realmMock).beginTransaction();
        doNothing().when(realmMock).commitTransaction();
    }

    @Test
    public void messageRepository_add() {
        String MESSAGE_UUID = "UUID";
        String MESSAGE_CONTENT = "CONTENT";
        String MESSAGE_INFO = "INFO";
        MessageModel message = new MessageModel();
        message.setUuid(MESSAGE_UUID);
        message.setContent(MESSAGE_CONTENT);
        message.setInfo(MESSAGE_INFO);

        when(databaseHelper.add(message)).thenReturn(message);

        TestSubscriber<String> tester = new TestSubscriber<>();
        messageRepository.add(message).subscribe(tester);

        verify(databaseHelper).add(message);

        tester.assertValue(MESSAGE_UUID);
        tester.assertCompleted();
        tester.assertNoErrors();
    }

    @Test
    public void messageRepository_findAll() {
        MessageModel message1 = MessageModel.newBuilder().content("CONTENT1").build();
        MessageModel message2 = MessageModel.newBuilder().content("CONTENT2").build();
        List<MessageModel> messages = Arrays.asList(message1, message2);

        when(databaseHelper.findAll(MessageModel.class)).thenReturn(messages);

        TestSubscriber<List<MessageModel>> tester = new TestSubscriber<>();
        messageRepository.findAll().subscribe(tester);

        assertEquals("invalid size", 2, messages.size());

        tester.assertValue(messages);
        tester.assertCompleted();
        tester.assertNoErrors();
    }

}