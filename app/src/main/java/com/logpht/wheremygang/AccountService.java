package com.logpht.wheremygang;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * Created by Long on 28/04/2018.
 */

public class AccountService {
    private static final String RESULT_SUCCESS = "success";
    private Context context;
    private String fullFileName;

    public AccountService(Context context) {
        this.context = context;
        this.fullFileName = context.getFilesDir().getAbsolutePath() + "/gang";
    }

    public AccountService() {
        this.context = null;
        this.fullFileName = null;
    }

    public User signIn(String userID, String password) {
        User result = null;
        /* implementation here */

        return result;
    }

    public String signUp(String id, String password, String name) {

        return null;
    }

    public boolean writeSavedAccount(User user) throws IOException {
        if (this.fullFileName != null) {
            FileWriter writer = new FileWriter(this.fullFileName);
            String s = user.getPhone() + "\n" +
                    user.getName() + "\n" +
                    user.getPassword() + "\n" +
                    user.getJoiningRoomID();
            writer.write(s);
            writer.close();
            return true;
        } else {
            return false;
        }
    }

    public boolean deleteSavedAccount() {
        if (this.fullFileName != null) {
            File file = new File(this.fullFileName);
            file.delete();
            return true;
        } else {
            return false;
        }
    }

    public User readSavedAccount() throws FileNotFoundException {
        if (this.fullFileName != null) {
            Scanner scanner = new Scanner(new File(this.fullFileName));
            User user = new User();
            try {
                user.setPhone(scanner.nextLine());
                user.setName(scanner.nextLine());
                user.setPassword(scanner.nextLine());
                user.setJoiningRoomID(Integer.parseInt(scanner.nextLine()));
                return user;
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }
}
