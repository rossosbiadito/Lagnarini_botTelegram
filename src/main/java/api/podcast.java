package api;

public class podcast {
    private String uuid;
    private String name;
    private String description;
    private String imageUrl;
    private String audioUrl;
    private String episodeName;

    // Getter e Setter
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }
    public String getEpisodeName() { return episodeName; }
    public void setEpisodeName(String episodeName) { this.episodeName = episodeName; }

    @Override
    public String toString() {
        String desc = (description != null) ? description.replaceAll("<[^>]*>", "") : "";
        if (desc.length() > 200) desc = desc.substring(0, 200) + "...";
        return "🎙 *" + name + "*\n\n" + desc;
    }
}