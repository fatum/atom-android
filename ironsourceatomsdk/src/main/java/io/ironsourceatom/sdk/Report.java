package io.ironsourceatom.sdk;

import android.content.Intent;

interface Report {
    Report setData(String value);
    Report setTable(String table);
    Report setToken(String token);
    Intent getIntent();
    void send();
}
