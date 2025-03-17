import pandas as pd
import matplotlib.pyplot as plt
import numpy as np

# Load CSV file
file_path = "data/comparison.csv"  # Update path if needed

try:
    data = pd.read_csv(file_path)
except FileNotFoundError:
    print(f"Error: File '{file_path}' not found!")
    exit()

# Extract values
playstore_values = data[data["Type"] == "Playstore"].iloc[:, 1:].values.flatten()
release_values = data[data["Type"] == "Release"].iloc[:, 1:].values.flatten()

categories = ["Splash", "Onboarding", "Home"]

# Define bar width and X positions
bar_width = 0.3
x = np.arange(len(categories))

# Create the plot
plt.figure(figsize=(8, 5))

# Plot bars for Playstore and Release
plt.bar(x - bar_width/2, playstore_values, bar_width, label="Playstore", color="blue")
plt.bar(x + bar_width/2, release_values, bar_width, label="Release", color="green")

# Add labels
plt.xlabel("Activity Type")
plt.ylabel("App Start Time (ms)")
plt.title("Comparison of App Start Time: Playstore vs Release")
plt.xticks(x, categories)

# Add values on top of bars
for i in range(len(categories)):
    plt.text(i - bar_width/2, playstore_values[i] + 5, f"{playstore_values[i]:.1f}", ha="center", fontsize=10, fontweight="bold", color="black")
    plt.text(i + bar_width/2, release_values[i] + 5, f"{release_values[i]:.1f}", ha="center", fontsize=10, fontweight="bold", color="black")

# Add legend
plt.legend()

# Save and display graph
output_path = "data/app_start_time_comparison.png"
plt.savefig(output_path, dpi=300, bbox_inches='tight')
print(f"Graph saved successfully at: {output_path}")

plt.show()
