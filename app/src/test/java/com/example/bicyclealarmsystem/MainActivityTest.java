package com.example.bicyclealarmsystem;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import android.widget.Button;

public class MainActivityTest {

    @InjectMocks
    private MainActivity mainActivity;

    @Mock
    private Button armButton;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInitialState() {
        assertFalse(mainActivity.isArmed());
    }

    @Test
    public void testArmAlarm() {
        mainActivity.armAlarm();
        assertTrue(mainActivity.isArmed());
        verify(armButton).setText("Disarm");
    }

    @Test
    public void testDisarmAlarm() {
        mainActivity.armAlarm();
        mainActivity.disarmAlarm();
        assertFalse(mainActivity.isArmed());
        verify(armButton).setText("Arm");
    }
}
