package br.com.fiap.precoauto.activities;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import br.com.fiap.precoauto.R;
import br.com.fiap.precoauto.VO.Marca;
import br.com.fiap.precoauto.VO.Modelo;
import br.com.fiap.precoauto.adapters.ModeloAdapter;

import com.google.gson.Gson;

public class ModeloActivity extends Activity implements
		SearchView.OnQueryTextListener, SearchView.OnCloseListener {
	private ListView myList;
	private SearchView searchView;
	private ModeloAdapter defaultAdapter;
	private List<Modelo> modelos;
	private String idMarca;
	private Marca marca;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_modelo);
		myList = (ListView) findViewById(R.id.listModelo);

		Intent i = getIntent();
		marca = (Marca) i.getSerializableExtra("marca");
		idMarca = marca.getId();
		
		modelos = new ArrayList<>();
		defaultAdapter = new ModeloAdapter(ModeloActivity.this, modelos);
		myList.setAdapter(defaultAdapter);
		myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				Intent i = new Intent(ModeloActivity.this, VersaoActivity.class);
				i.putExtra("marca", marca);
				i.putExtra("modelo", ( (Modelo) defaultAdapter.getItem(position)));
				startActivity(i);
			}
		});

		this.init();

		searchView = (SearchView) findViewById(R.id.searchModelo);

		searchView.setIconifiedByDefault(false);

		searchView.setOnQueryTextListener(this);
		searchView.setOnCloseListener(this);
	}

	@Override
	public boolean onClose() {
		myList.setAdapter(defaultAdapter);
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		displayResults(query);
		return false;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		if (!newText.isEmpty()) {
			displayResults(newText);
		} else {
			init();
		}

		return false;
	}

	private void displayResults(String query) {
		List<Modelo> modeloAux =  new ArrayList<>();
		
		for (Modelo modelo : modelos) {
			if (modelo.getNome().toLowerCase().trim().contains(query)) {
				modeloAux.add(modelo);
			}
		}
		
		defaultAdapter = new ModeloAdapter(ModeloActivity.this, modeloAux);
		myList.setAdapter(defaultAdapter);


	}

	public void init() {
		new HttpRequestTask().execute();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_refresh) {
			this.init();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private class HttpRequestTask extends AsyncTask<Void, Void, String> {
		@Override
		protected String doInBackground(Void... params) {
			String urlString = "http://zillius.com.br/fipe/modelos/"+ idMarca;
			String resultToDisplay = "";

			InputStream in = null;

			try {

				URL url = new URL(urlString);

				HttpURLConnection urlConnection = (HttpURLConnection) url
						.openConnection();

				in = new BufferedInputStream(urlConnection.getInputStream());

				BufferedReader br = new BufferedReader(
						new InputStreamReader(in));

				resultToDisplay = br.readLine();

				br.close();

			} catch (Exception e) {

				System.out.println(e.getMessage());

				return e.getMessage();

			}

			return resultToDisplay;
		}

		@Override
		protected void onPostExecute(String json) {

			Gson gson = new Gson();
			Modelo[] modelosArray = gson.fromJson(json, Modelo[].class);
			modelos = Arrays.asList(modelosArray);
			defaultAdapter = new ModeloAdapter(ModeloActivity.this, modelos);
			myList.setAdapter(defaultAdapter);

		}

	}

}
