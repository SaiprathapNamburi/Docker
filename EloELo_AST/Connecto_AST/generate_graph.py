import pandas as pd
import matplotlib.pyplot as plt

# Load CSV file
file_path = "data/comparison.csv"  # Update path if needed

try:
    data = pd.read_csv(file_path)
except FileNotFoundError:
    print(f"Error: File '{file_path}' not found!")
    exit()

# Check if required columns exist
if "Type" not in data.columns or "Average App Start Time (ms)" not in data.columns:
    print("Error: CSV does not have the required 'Type' and 'Average App Start Time (ms)' columns!")
    exit()

# Extract values
playstore_time = data[data["Type"] == "Playstore"]["Average App Start Time (ms)"].values[0]
release_time = data[data["Type"] == "Release"]["Average App Start Time (ms)"].values[0]

# Calculate difference as (Release - Playstore)
time_difference = release_time - playstore_time

# Define colors: Green for lower time, Red for higher time
colors = ["green" if playstore_time < release_time else "red", 
          "green" if release_time < playstore_time else "red"]

# Create the plot with reduced bar width
plt.figure(figsize=(6, 4))
plt.bar(["Playstore", "Release"], [playstore_time, release_time], color=colors, width=0.4)  # Reduced bar width

plt.xlabel("Version")
plt.ylabel("Average App Start Time (ms)")
plt.title("Comparison of Playstore vs Release App Start Time")

# Add values on top of bars
for i, v in enumerate([playstore_time, release_time]):
    plt.text(i, v + 10, f"{v:.1f} ms", ha="center", fontsize=12, fontweight="bold")

# Adjust the position of the difference text
mid_x = 0.5  # Middle point between Playstore and Release bars
mid_y = min(playstore_time, release_time) + abs(time_difference) / 2  # Lower position

# Display the difference with +/- sign
diff_text = f"Difference: {time_difference:+.1f} ms"  # +ve or -ve sign
diff_color = "green" if time_difference < 0 else "red"  # Green if faster, Red if slower

plt.text(mid_x, mid_y, diff_text, ha="center", 
         fontsize=12, fontweight="bold", color=diff_color)

# Save the graph as an image
output_path = "data/app_start_time_comparison.png"
plt.savefig(output_path, dpi=300, bbox_inches='tight')
print(f"Graph saved successfully at: {output_path}")

# Show the graph
plt.show()
