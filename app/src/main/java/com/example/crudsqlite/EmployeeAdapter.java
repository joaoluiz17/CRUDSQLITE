package com.example.crudsqlite;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.List;

public class EmployeeAdapter extends ArrayAdapter<Employee> {

    Context mCtx;
    int layoutRes;
    List<Employee> employeeList;
    SQLiteDatabase mDatabase;

    public EmployeeAdapter(Context mCtx, int layoutRes, List<Employee> employeeList, SQLiteDatabase mDatabase){
        super(mCtx, layoutRes, employeeList);

        this.mCtx = mCtx;
        this.layoutRes = layoutRes;
        this.employeeList = employeeList;
        this.mDatabase = mDatabase;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from (mCtx);

        View view = inflater.inflate (layoutRes, null);

        TextView textViewName = view.findViewById (R.id.textViewName);
        TextView textViewDept = view.findViewById (R.id.textViewDepartment);
        TextView textViewSalary = view.findViewById (R.id.textViewSalary);
        TextView textViewJoiningDate = view.findViewById (R.id.textViewJoiningDate);

        final Employee employee = employeeList.get (position);

        textViewName.setText (employee.getName ());
        textViewDept.setText (employee.getDept ());
        textViewSalary.setText (String.valueOf (employee.getSalary ()));
        textViewJoiningDate.setText (employee.getJoiningdate ());

        view.findViewById (R.id.buttonDeleteEmployee).setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                deleteEmployee(employee);
            }
        });

        view.findViewById (R.id.buttonEditEmployee).setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                updateEmployee(employee);
            }
        });

        return view;
    }

    public void updateEmployee(final Employee employee){
        AlertDialog.Builder builder = new AlertDialog.Builder (mCtx);
        LayoutInflater inflater = LayoutInflater.from (mCtx);
        View view = inflater.inflate (R.layout.dialog_update_employee, null);
        builder.setView (view);

        final AlertDialog alertDialog = builder.create ();
        alertDialog.show ();

        final EditText editTextName = view.findViewById (R.id.editTextName);
        final EditText editTextSalary = view.findViewById (R.id.editTextSalary);
        final Spinner spinner = view.findViewById (R.id.spinnerDepartment);

        editTextName.setText (employee.getName ());
        editTextSalary.setText (String.valueOf (employee.getSalary ()));



        view.findViewById (R.id.buttonUpdateEmployee).setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {

                String name = editTextName.getText ().toString ().trim ();
                String salary = editTextSalary.getText ().toString ().trim ();
                String dept = spinner.getSelectedItem ().toString ().trim ();

                if(name.isEmpty ()){
                    editTextName.setError ("Nome está vazio");
                    editTextName.requestFocus ();
                    return;
                }

                if(salary.isEmpty ()){
                    editTextSalary.setError ("Salario está vazio");
                    editTextSalary.requestFocus ();
                    return;
                }

                String sql = "UPDATE employees \n" +
                        "SET name = ?, \n" +
                        "department = ?, \n" +
                        "salary = ? \n" +
                        "WHERE id = ?;\n";

                mDatabase.execSQL (sql,new String[]{name, dept, salary, String.valueOf (employee.getId ())});

                Toast.makeText (mCtx, "Funcionario atualizado", Toast.LENGTH_SHORT).show ();

                loadEmployeesFromDatabaseAgain();

                alertDialog.dismiss ();
            }
        });

    }

    private void deleteEmployee(final Employee employee){
        AlertDialog.Builder builder = new AlertDialog.Builder (mCtx);
        builder.setTitle ("Você tem certeza?");

        builder.setPositiveButton ("Sim", new DialogInterface.OnClickListener () {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
             String sql = "DELETE FROM employees WHERE id = ?";
             mDatabase.execSQL (sql, new Integer[]{employee.getId ()});
             loadEmployeesFromDatabaseAgain ();
            }
        });

        builder.setNegativeButton ("Cancelar", new DialogInterface.OnClickListener () {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        AlertDialog alertDialog = builder.create ();
        alertDialog.show ();
    }

    private void loadEmployeesFromDatabaseAgain() {
        String sql = "SELECT * FROM employees";

        Cursor cursor = mDatabase.rawQuery (sql, null);

        if (cursor.moveToFirst ()){
            employeeList.clear ();
            do{
                employeeList.add (new Employee (
                        cursor.getInt (0),
                        cursor.getString (1),
                        cursor.getString (2),
                        cursor.getString (3),
                        cursor.getDouble (4)

                ));

            } while (cursor.moveToNext ());

            notifyDataSetChanged ();
        }
    }
}
