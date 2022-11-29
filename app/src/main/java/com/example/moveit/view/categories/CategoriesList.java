package com.example.moveit.view.categories;

import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.example.moveit.R;
import com.example.moveit.model.categories.Category;
import com.example.moveit.model.categories.CategoryListAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Objects;

public class CategoriesList extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private ArrayAdapter<Category> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories_list);
        setTitle(getString(R.string.activity_list_title));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;

        setUpAddCategoryBtn();
        setUpCategoryList();
    }

    private void setUpCategoryList() {
        ListView categoryListView = findViewById(R.id.categoryList);
        categoryListView.setEmptyView(findViewById(R.id.emptyTV));

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading...");
        progressDialog.show();

        adapter = new CategoryListAdapter(CategoriesList.this, R.layout.item_category);
        categoryListView.setAdapter(adapter);

        db.collection("categories").document(currentUser.getUid()).collection("categoryList")
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Category> categoryList = new ArrayList<>();
                        for (QueryDocumentSnapshot categoryDoc : Objects.requireNonNull(task.getResult())) {
                            Category currentCategory = categoryDoc.toObject(Category.class);
                            categoryList.add(currentCategory);
                        }

                        adapter.clear();
                        adapter.addAll(categoryList);
                        progressDialog.dismiss();
                    } else {
                            Log.d("CategoriesList", "Error retrieving documents: ", task.getException());
                    }
                });
    }

    private void setUpAddCategoryBtn() {
        FloatingActionButton addNewCategoryBtn = findViewById(R.id.addNewCategoryBtn);
        addNewCategoryBtn.setOnClickListener(v -> startActivity(new Intent(CategoriesList.this, AddCategory.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpCategoryList();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemID = item.getItemId();

        if (itemID == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}