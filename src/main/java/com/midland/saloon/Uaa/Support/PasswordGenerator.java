package com.midland.saloon.Uaa.Support;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The first password an account gets.
 *
 * It used to be the person's surname, which meant anyone who knew their name
 * knew their password. That was only ever the case because there was no way
 * to tell someone a password they could not have guessed; SMS removes that
 * reason, so the password is now random.
 *
 * Two things shape the alphabet. It leaves out the characters that get misread
 * off a phone screen - O and 0, l and 1 and I - because the person has to type
 * this by hand from a text message. And it keeps to plain ASCII letters and
 * digits, so nothing depends on which keyboard their phone gives them.
 *
 * It is a starting password, not a permanent one: the account is flagged so
 * that the only thing this password can do is be replaced.
 */
public final class PasswordGenerator {

    private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";   // no I, O
    private static final String LOWER = "abcdefghijkmnpqrstuvwxyz";   // no l, o
    private static final String DIGITS = "23456789";                  // no 0, 1

    private static final int LENGTH = 8;

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordGenerator() {
    }

    public static String generate() {
        String all = UPPER + LOWER + DIGITS;

        // One of each kind up front, so a password can never come out as
        // eight lowercase letters by chance and fail a rule elsewhere.
        List<Character> characters = new ArrayList<>(LENGTH);
        characters.add(pick(UPPER));
        characters.add(pick(LOWER));
        characters.add(pick(DIGITS));
        while (characters.size() < LENGTH) {
            characters.add(pick(all));
        }

        // Without this the shape would always be upper-lower-digit-then-noise.
        Collections.shuffle(characters, RANDOM);

        StringBuilder password = new StringBuilder(LENGTH);
        characters.forEach(password::append);
        return password.toString();
    }

    private static char pick(String alphabet) {
        return alphabet.charAt(RANDOM.nextInt(alphabet.length()));
    }
}
