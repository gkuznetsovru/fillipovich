package ru.ovod.CarInspection;

import android.content.Context;
import android.content.SharedPreferences;


// Класс для управления настройками приложения

public class SettingsHelper {

    private Context context;
    // Внимание.

    // обращение к переменным только через Getter и Setter !
    private Boolean show_synchronized_acts = false; // показывать ли синхронизированные акты. Устанавливать через  setShow_synchronized_acts !!
    private Integer counter_cols = 4; // количество колонок в фото-плитке

    private static final String APP_PREFERENCES = "ovodsettings";  // название блока с нашими настройка
    private static final String APP_PREFERENCES_COUNTER_COLS = "countercols"; // настройка -  сколько колонок
    private static final String APP_PREFERENCES_SHOWSYNCACTS = "showsyncacts"; // настройка -   показывать ли синхронизированные акты
    private SharedPreferences mSettings;


    // конструктор - context надо передать при созданиии
    // пример:  settingsHelper = new SettingsHelper(getApplicationContext());
    public SettingsHelper(Context context) {
        this.context = context;
        mSettings = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
    }


    // далее идут Getters и Setters по нашим переменным


    public Boolean getShow_synchronized_acts() {

        show_synchronized_acts = true; // по умолчанию пусть показываются
        if (mSettings.contains(APP_PREFERENCES_SHOWSYNCACTS)) {
            // Получаем число из настроек
            show_synchronized_acts = mSettings.getBoolean(APP_PREFERENCES_SHOWSYNCACTS, true);
        }
        return show_synchronized_acts;

    }

    public void setShow_synchronized_acts(Boolean show_synchronized_acts) {
        this.show_synchronized_acts = show_synchronized_acts;

        // Запоминаем данные
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putBoolean(APP_PREFERENCES_SHOWSYNCACTS, this.show_synchronized_acts );
        editor.apply();
    }


    public Integer getCounter_cols() {
        counter_cols = 4; // по умолчанию пусть показываются
        if (mSettings.contains(APP_PREFERENCES_COUNTER_COLS)) {
            // Получаем число из настроек
            counter_cols = (Integer) mSettings.getInt(APP_PREFERENCES_COUNTER_COLS, 4);
        }
        return counter_cols;
    }

    public void setCounter_cols(Integer counter_cols) {
        this.counter_cols = counter_cols;
        // Запоминаем данные
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putInt (APP_PREFERENCES_COUNTER_COLS, this.counter_cols );
        editor.apply();
    }
}
