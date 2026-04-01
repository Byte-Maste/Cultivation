package com.example.cultivation.utils;

import com.example.cultivation.data.Plot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CropCalendar {

    public static List<Task> getTasksForPlot(Plot plot) {
        List<Task> tasks = new ArrayList<>();
        if (plot.plantingDate == null || plot.plantingDate.isEmpty())
            return tasks;

        long daysOld = getDaysDifference(plot.plantingDate);
        String variety = plot.cropVariety != null ? plot.cropVariety : "Unknown";

        // Logic based on Days Old (Simplified for Demo)
        // General Logic for most Sugarcane
        if (daysOld >= 0 && daysOld <= 3) {
            tasks.add(new Task("Initial Irrigation", "Apply light irrigation immediately after planting.", plot.name,
                    "Today", true));
        } else if (daysOld >= 30 && daysOld <= 45) {
            String desc = variety.contains("0238") ? "Apply 1st dose of Nitrogen (Urea). This variety feeds heavy."
                    : "Apply basic Urea dose.";
            tasks.add(new Task("Fertilizer Application", desc, plot.name, "Due Now", true));
        } else if (daysOld >= 45 && daysOld <= 60) {
            tasks.add(new Task("Weeding & Hoeing", "Control weeds to prevent competition.", plot.name, "This Week",
                    false));
        } else if (daysOld >= 90 && daysOld <= 100) {
            tasks.add(new Task("Earthing Up", "Support the crop with soil banking.", plot.name, "Upcoming", false));
        }

        // Default task if nothing specific matches (to ensure list isn't always empty
        // for demo)
        // Only if it's a "Demo" plot (older than 100 days or weird date)
        if (tasks.isEmpty() && daysOld > 0 && daysOld < 365) {
            tasks.add(new Task("Routine Inspection", "Check for pests like Borers.", plot.name, "Routine", false));
        }

        return tasks;
    }

    private static long getDaysDifference(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date plantingDate = sdf.parse(dateStr);
            Date today = new Date();
            long diff = today.getTime() - plantingDate.getTime();
            return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return -1;
        }
    }
}
