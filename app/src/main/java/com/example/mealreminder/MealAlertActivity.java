import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MealAlertActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Meal Reminder 🍽️")
                .setMessage("It's time for your next meal!")
                .setPositiveButton("Okay", (dialog, which) -> finish())
                .setNegativeButton("Snooze", (dialog, which) -> snoozeReminder())
                .setCancelable(false)
                .show();
    }

    private void snoozeReminder() {
        Toast.makeText(this, "Reminder Snoozed for 10 minutes", Toast.LENGTH_SHORT).show();
        finish();
    }
}
