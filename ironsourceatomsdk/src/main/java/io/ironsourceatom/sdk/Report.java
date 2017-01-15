package io.ironsourceatom.sdk;

import android.os.Bundle;

public interface Report {

	Report setData(String value);

	Report setTable(String table);

	Report setToken(String token);

	Report setEndPoint(String endpoint);

	Report setBulk(boolean b);

	Bundle getExtras();
}
