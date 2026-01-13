package api;

public class podcast {
    private String uuid;
    private String name;
    private String description;
    private String imageUrl;

    // Getter e Setter
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    @Override
    public String toString() {
        String cleanDescription = "";
        if (description != null) {
            cleanDescription = description.replaceAll("<[^>]*>", "");
            cleanDescription = cleanDescription.replace("&nbsp;", " ").trim();
        }
        if (cleanDescription.length() > 800) {
            cleanDescription = cleanDescription.substring(0, 800) + "...";
        }

        return "🎙 *" + name + "*\n\n" + cleanDescription;
    }
}
