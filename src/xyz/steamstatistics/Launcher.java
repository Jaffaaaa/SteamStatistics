package xyz.steamstatistics;

import javax.security.auth.login.LoginException;

public class Launcher {

    public static void main(String[] args) throws LoginException {
        new Core().start(args[0], args[1], args[2], args[3]);
    }

}
