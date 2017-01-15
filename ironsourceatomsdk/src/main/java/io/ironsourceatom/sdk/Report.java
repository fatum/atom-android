package io.ironsourceatom.sdk;

import android.content.Intent;

public interface Report {

	Report setData(String value);

	Report setTable(String table);

	Report setToken(String token);

	Report setEndPoint(String endpoint);

	Report setBulk(boolean b);

	Intent getIntent();

	void send();

}
