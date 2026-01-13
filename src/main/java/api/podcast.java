package api;

public class podcast {
    // Identificatore univoco del podcast
    private String uuid;
    // Nome del podcast
    private String name;
    // Descrizione del podcast, può contenere tag HTML
    private String description;
    // URL dell'immagine del podcast
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
        // Pulizia della descrizione dai tag HTML
        String cleanDescription = "";
        if (description != null) {
            cleanDescription = description.replaceAll("<[^>]*>", ""); // rimuove tag HTML
            cleanDescription = cleanDescription.replace("&nbsp;", " ").trim(); // sostituisce spazi non standard
        }

        // Limita la descrizione a 800 caratteri per evitare messaggi troppo lunghi
        if (cleanDescription.length() > 800) {
            cleanDescription = cleanDescription.substring(0, 800) + "...";
        }

        // Ritorna una stringa formattata con nome e descrizione pulita
        return "🎙 *" + name + "*\n\n" + cleanDescription;
    }
}
