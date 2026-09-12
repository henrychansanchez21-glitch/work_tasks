package com.example.data

data class VideoItem(
    val id: String,
    val title: String,
    val description: String,
    val videoUrl: String,
    val thumbnailUrl: String,
    val duration: String,
    val category: String,
    val author: String,
    val views: String,
    val resolution: String = "1080p FHD",
    val isFavorite: Boolean = false,
    val isCustom: Boolean = false
)

object VideoRepository {
    val sampleVideos: List<VideoItem> = listOf(
        VideoItem(
            id = "vid_1",
            title = "Big Buck Bunny - 4K Remaster",
            description = "Un conejo gigante se enfrenta a tres roedores traviesos que acosan a las criaturas inocentes del bosque.",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/BigBuckBunny.jpg",
            duration = "09:56",
            category = "Animación",
            author = "Blender Open Studio",
            views = "4.2M vistas",
            resolution = "4K Ultra HD"
        ),
        VideoItem(
            id = "vid_2",
            title = "Tears of Steel - Sci-Fi Cyberpunk",
            description = "En un futuro distópico, un grupo de científicos y guerreros intenta salvar la Tierra usando tecnología cibernética avanzada.",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/TearsOfSteel.jpg",
            duration = "12:14",
            category = "Tecnología",
            author = "Mango VFX Team",
            views = "2.8M vistas",
            resolution = "1080p 60fps"
        ),
        VideoItem(
            id = "vid_3",
            title = "Sintel - The Dragon Quest",
            description = "Una solitaria guerrera emprende un épico y peligroso viaje para rescatar a un cachorro de dragón que crió con amor.",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
            thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/Sintel.jpg",
            duration = "14:48",
            category = "Animación",
            author = "Durian Open Movie Project",
            views = "5.6M vistas",
            resolution = "1080p FHD"
        ),
        VideoItem(
            id = "vid_4",
            title = "Elephant's Dream - Realidad Mecánica",
            description = "Un viaje surrealista a través de una colosal máquina viviente dirigida por los personajes Proog y Emo.",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ElephantsDream.jpg",
            duration = "10:54",
            category = "Destacados",
            author = "Orange Animation Studio",
            views = "1.9M vistas",
            resolution = "1080p FHD"
        ),
        VideoItem(
            id = "vid_5",
            title = "For Bigger Blazes - Extreme Performance",
            description = "Demostración de alto impacto cinematográfico y pruebas de rendimiento visual en escenas de acción intensa.",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerBlazes.jpg",
            duration = "00:15",
            category = "Destacados",
            author = "Cinema Lab HD",
            views = "890K vistas",
            resolution = "4K HDR"
        ),
        VideoItem(
            id = "vid_6",
            title = "For Bigger Escape - Naturaleza Silvestre",
            description = "Impresionantes tomas aéreas de paisajes vírgenes, acantilados y la fauna libre en su hábitat natural.",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerEscapes.jpg",
            duration = "00:15",
            category = "Naturaleza",
            author = "Wild Planet Films",
            views = "1.1M vistas",
            resolution = "4K Ultra HD"
        ),
        VideoItem(
            id = "vid_7",
            title = "For Bigger Fun - Diversión y Deportes",
            description = "Compilación de momentos enérgicos al aire libre y deportes de aventura con ritmo dinámico.",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
            thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ForBiggerFun.jpg",
            duration = "00:60",
            category = "Destacados",
            author = "Action Pulse Studio",
            views = "720K vistas",
            resolution = "1080p 60fps"
        ),
        VideoItem(
            id = "vid_8",
            title = "We Are Going On Bullrun - Rally & Supercars",
            description = "Velocidad pura y caravana de superdeportivos recorriendo carreteras de alta velocidad con sonido envolvente.",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
            thumbnailUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/WeAreGoingOnBullrun.jpg",
            duration = "00:47",
            category = "Tecnología",
            author = "Motor Trend Vault",
            views = "650K vistas",
            resolution = "1080p FHD"
        )
    )

    val categories = listOf("Todos", "Destacados", "Animación", "Tecnología", "Naturaleza", "Favoritos", "Mis Videos")
}
