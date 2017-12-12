package org.ladbury.userInterfacePkg;

import org.ladbury.smartpowerPkg.SmartPower;

public class UiLogger
{
    public static void displayString(String logEntry)
    {
        SmartPower.getInstance().getFrame().displayLog(logEntry);
    }
}
