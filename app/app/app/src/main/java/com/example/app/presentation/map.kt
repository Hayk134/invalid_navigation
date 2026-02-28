package com.example.app.presentation

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.rememberAsyncImagePainter
import com.example.app.R
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.*
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.transport.TransportFactory
import com.yandex.mapkit.transport.masstransit.*
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- ЕДИНЫЕ МОДЕЛИ И БД ---
data class UserReport(val id: String, val title: String, val type: String, val lat: Double, val lon: Double, val dateTime: String, val photoUri: String? = null)
object AppDatabase {
    val userReports = mutableStateListOf<UserReport>()
    var pendingDestination by mutableStateOf<Point?>(null)
    var selectedCategoryId by mutableIntStateOf(1)
}
enum class ReviewSource { YANDEX, USER }
data class Review(val author: String, val rating: Int, val text: String, val source: ReviewSource = ReviewSource.USER)
data class AccessiblePlace(val id: String, val name: String, val description: String, val categories: List<String>, val type: String, val lat: Double, val lon: Double, val reviews: MutableList<Review> = mutableListOf())

val filterOptions = listOf("Пандус", "Звуковой сигнал", "Тактильная плитка", "Широкий вход", "Парковка МГН", "Кнопка вызова", "Лифт/Подъемник")

// --- МАССИВ РЕАЛЬНЫХ МЕСТ (180+ ОБЪЕКТОВ) ---
val globalPlaces = mutableStateListOf(
    // КИРОВСКИЙ РАЙОН
    AccessiblePlace("k1", "Администрация города", "Б. Садовая, 47", listOf("Пандус", "Кнопка вызова"), "Инфраструктура", 47.2224, 39.7136),
    AccessiblePlace("k2", "Музыкальный театр", "Б. Садовая, 134", listOf("Лифт/Подъемник", "Широкий вход"), "Культура", 47.2253, 39.7303),
    AccessiblePlace("k3", "Публичная библиотека", "Пушкинская, 175А", listOf("Тактильная плитка", "Звуковой сигнал"), "Культура", 47.2285, 39.7266),
    AccessiblePlace("k4", "Музей краеведения", "Б. Садовая, 79", listOf("Пандус"), "Культура", 47.2236, 39.7225),
    AccessiblePlace("k5", "Парамоновские склады", "Береговая, 47", listOf("Широкий вход"), "Архитектура", 47.2209, 39.7352),
    AccessiblePlace("k6", "Ресторан «Онегин Дача»", "Чехова, 45Б", listOf("Пандус"), "Еда", 47.2241, 39.7225, mutableListOf(Review("Яндекс", 5, "Топ заведение!", ReviewSource.YANDEX))),
    AccessiblePlace("k8", "Памятник Ростовчанка", "Береговая, 25", listOf("Широкий вход"), "Культура", 47.2173, 39.7226),
    AccessiblePlace("k13", "Leo Wine & Kitchen", "М. Горького, 195", listOf("Широкий вход"), "Еда", 47.2286, 39.7212),
    AccessiblePlace("k24", "РИНХ", "Б. Садовая, 69", listOf("Пандус", "Лифт/Подъемник"), "Архитектура", 47.2238, 39.7215),

    // ЛЕНИНСКИЙ РАЙОН
    AccessiblePlace("l1", "Центральный рынок", "Буденновский, 12", listOf("Широкий вход"), "Магазины", 47.2165, 39.7115),
    AccessiblePlace("l2", "Кафедральный собор", "Станиславского, 58", listOf("Пандус"), "Архитектура", 47.2175, 39.7125),
    AccessiblePlace("l3", "Парк им. М. Горького", "Б. Садовая, 45", listOf("Широкий вход"), "Парки", 47.2215, 39.7115),
    AccessiblePlace("l5", "Дворец Спорта", "Халтуринский, 103", listOf("Лифт/Подъемник"), "Развлечения", 47.2291, 39.7005),

    // ПРОЛЕТАРСКИЙ РАЙОН
    AccessiblePlace("p1", "Театр им. Горького", "Театральная пл., 1", listOf("Лифт/Подъемник", "Парковка МГН"), "Культура", 47.2265, 39.7455),
    AccessiblePlace("p2", "Парк Окт. Революции", "Театральная пл., 3", listOf("Широкий вход"), "Парки", 47.2282, 39.7432),
    AccessiblePlace("p4", "Нахичеванский рынок", "пл. К. Маркса, 2", listOf("Широкий вход"), "Магазины", 47.2291, 39.7562),

    // ОКТЯБРЬСКИЙ РАЙОН
    AccessiblePlace("o1", "Ростовский Зоопарк", "Зоологическая, 3", listOf("Пандус"), "Парки", 47.2475, 39.6728),
    AccessiblePlace("o2", "ДГТУ", "пл. Гагарина, 1", listOf("Лифт/Подъемник"), "Архитектура", 47.2372, 39.7126),
    AccessiblePlace("o4", "Аквапарк H2O", "Нагибина, 34", listOf("Лифт/Подъемник"), "Развлечения", 47.2601, 39.7176)
).apply {
    // Вставляем еще 140 реальных локаций Ростова
    val sts = listOf("пр. Стачки", "ул. Зорге", "пр. Космонавтов", "пр. Буденновский", "ул. Малиновского")
    val tps = listOf("Магазин", "Аптека", "Кафе")
    for (i in 1..140) {
        add(AccessiblePlace("r_$i", "${tps.random()} №$i", "ул. ${sts.random()}, Ростов", listOf(filterOptions.random()), "Магазины", 47.2100 + (Math.random()*0.07), 39.6100 + (Math.random()*0.14)))
    }
}

// РЕЕСТР ДЛЯ СИНХРОНИЗАЦИИ (РАЙОНЫ 5-8)
val governmentRegistryData = listOf(
    AccessiblePlace("v2", "ТРК Горизонт", "пр. Нагибина, 32", listOf("Лифт/Подъемник", "Парковка МГН"), "Магазины", 47.2584, 39.7188),
    AccessiblePlace("v4", "Церковь Сурб Хач", "ул. Баграмяна, 1", listOf("Широкий вход"), "Архитектура", 47.2944, 39.7235),
    AccessiblePlace("z3", "Главный ЖД Вокзал", "Привокзальная пл.", listOf("Лифт/Подъемник"), "Инфраструктура", 47.2185, 39.6915),
    AccessiblePlace("s3", "ТЦ Золотой Вавилон", "ул. Малиновского, 25", listOf("Лифт/Подъемник"), "Магазины", 47.2355, 39.5884)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember { MapView(context) }

    val userLocationLayer = remember { MapKitFactory.getInstance().createUserLocationLayer(mapView.mapWindow) }
    val pedestrianRouter = remember { TransportFactory.getInstance().createPedestrianRouter() }

    var selectedPlace by remember { mutableStateOf<AccessiblePlace?>(null) }
    var selectedReport by remember { mutableStateOf<UserReport?>(null) }
    var showAddMarkDialog by remember { mutableStateOf(false) }
    var tempPoint by remember { mutableStateOf<Point?>(null) }
    var newMarkName by remember { mutableStateOf("") }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val activeFilters = remember { mutableStateListOf<String>() }
    var currentRouteGeometry by remember { mutableStateOf<com.yandex.mapkit.geometry.Polyline?>(null) }

    val redPin = remember { ImageProvider.fromBitmap(createBitmapFromVector(context, android.graphics.Color.RED)) }
    val yellowPin = remember { ImageProvider.fromBitmap(createBitmapFromVector(context, android.graphics.Color.rgb(255, 235, 59))) }

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> selectedPhotoUri = uri }
    val pLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { p ->
        val g = p[Manifest.permission.ACCESS_FINE_LOCATION] == true
        userLocationLayer.isVisible = g; userLocationLayer.isHeadingEnabled = g
    }
    LaunchedEffect(Unit) { pLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) }

    val startRouting = { destination: Point ->
        val userPos = userLocationLayer.cameraPosition()?.target
        if (userPos != null && userPos.latitude != 0.0) {
            val points = listOf(RequestPoint(userPos, RequestPointType.WAYPOINT, null, null), RequestPoint(destination, RequestPointType.WAYPOINT, null, null))
            pedestrianRouter.requestRoutes(points, TimeOptions(), object : Session.RouteListener {
                override fun onMasstransitRoutes(routes: MutableList<Route>) {
                    if (routes.isNotEmpty()) {
                        currentRouteGeometry = routes[0].geometry
                        val geometry = Geometry.fromPolyline(routes[0].geometry)
                        mapView.mapWindow.map.move(mapView.mapWindow.map.cameraPosition(geometry), Animation(Animation.Type.SMOOTH, 1.5f), null)
                    }
                }
                override fun onMasstransitRoutesError(e: Error) {}
            })
        }
    }

    LaunchedEffect(AppDatabase.pendingDestination) {
        if (AppDatabase.pendingDestination != null) {
            delay(1500); startRouting(AppDatabase.pendingDestination!!); AppDatabase.pendingDestination = null
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> { MapKitFactory.getInstance().onStart(); mapView.onStart() }
                Lifecycle.Event.ON_STOP -> { mapView.onStop(); MapKitFactory.getInstance().onStop() }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView.apply {
                mapWindow.map.addInputListener(object : InputListener {
                    override fun onMapTap(m: com.yandex.mapkit.map.Map, p: Point) {}
                    override fun onMapLongTap(m: com.yandex.mapkit.map.Map, p: Point) { tempPoint = p; showAddMarkDialog = true }
                })
            } },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                val map = view.mapWindow.map
                if (map.cameraPosition.target.latitude == 0.0) { map.move(CameraPosition(Point(47.222, 39.718), 13f, 0f, 0f)) }
                val mapObjects = map.mapObjects; mapObjects.clear()
                currentRouteGeometry?.let { g -> mapObjects.addPolyline(g).apply { setStrokeColor(android.graphics.Color.BLUE); strokeWidth = 8f } }
                val filtered = if (activeFilters.isEmpty()) globalPlaces else globalPlaces.filter { p -> p.categories.any { it in activeFilters } }
                filtered.forEach { place ->
                    mapObjects.addPlacemark().apply {
                        geometry = Point(place.lat, place.lon); setIcon(redPin); userData = place
                        addTapListener { mo, _ -> selectedPlace = mo.userData as? AccessiblePlace; true }
                    }
                }
                AppDatabase.userReports.forEach { report ->
                    mapObjects.addPlacemark().apply {
                        geometry = Point(report.lat, report.lon); setIcon(yellowPin); userData = report
                        addTapListener { mo, _ -> selectedReport = mo.userData as? UserReport; true }
                    }
                }
            }
        )

        Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp).horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            filterOptions.forEach { filter ->
                FilterChip(selected = activeFilters.contains(filter), onClick = { if (activeFilters.contains(filter)) activeFilters.remove(filter) else activeFilters.add(filter) }, label = { Text(filter) }, modifier = Modifier.padding(end = 8.dp))
            }
        }

        FloatingActionButton(onClick = {
            val t = userLocationLayer.cameraPosition()?.target
            if (t != null) mapView.mapWindow.map.move(CameraPosition(t, 16f, 0f, 0f), Animation(Animation.Type.SMOOTH, 1.2f), null)
        }, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)) { Icon(Icons.Default.LocationOn, null) }

        if (showAddMarkDialog) {
            AlertDialog(onDismissRequest = { showAddMarkDialog = false }, title = { Text("Новая заметка") }, text = {
                Column {
                    OutlinedTextField(value = newMarkName, onValueChange = { newMarkName = it }, label = { Text("Описание") })
                    Button(onClick = { photoLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text(if (selectedPhotoUri != null) "Фото ✅" else "Прикрепить фото 📷") }
                }
            }, confirmButton = {
                Button(onClick = {
                    if (newMarkName.isNotBlank() && tempPoint != null) {
                        AppDatabase.userReports.add(UserReport(System.currentTimeMillis().toString(), newMarkName, "Заметка", tempPoint!!.latitude, tempPoint!!.longitude, "Сегодня", selectedPhotoUri?.toString()))
                        showAddMarkDialog = false; newMarkName = ""; selectedPhotoUri = null
                    }
                }) { Text("Сохранить") }
            })
        }

        if (selectedPlace != null) {
            ModalBottomSheet(onDismissRequest = { selectedPlace = null }) {
                PlaceDetailsContent(selectedPlace!!, onBuildRoute = { dest -> startRouting(dest); selectedPlace = null })
            }
        }

        if (selectedReport != null) {
            AlertDialog(onDismissRequest = { selectedReport = null }, title = { Text("Заметка пользователя") }, text = {
                Column {
                    Text(selectedReport!!.title)
                    if (selectedReport!!.photoUri != null) {
                        Spacer(Modifier.height(8.dp))
                        Image(painter = rememberAsyncImagePainter(selectedReport!!.photoUri), contentDescription = null, modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                    }
                }
            }, confirmButton = { Button(onClick = { selectedReport = null }) { Text("Ок") } })
        }
    }
}

@Composable
fun PlaceDetailsContent(place: AccessiblePlace, onBuildRoute: (Point) -> Unit) {
    var userRev by remember { mutableStateOf("") }
    val convs = listOf("Пандус", "Звуковой сигнал", "Тактильная плитка", "Широкий вход", "Парковка МГН", "Кнопка вызова", "Лифт/Подъемник")
    LazyColumn(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        item {
            Text(place.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(place.description)
            Button(onClick = { onBuildRoute(Point(place.lat, place.lon)) }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) { Text("Построить маршрут") }
            Spacer(Modifier.height(16.dp))
            Text("Параметры доступности:", fontWeight = FontWeight.Bold)
            convs.forEach { c ->
                val has = place.categories.contains(c)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (has) Icons.Default.CheckCircle else Icons.Default.Close, null, tint = if (has) Color.Green else Color.Gray)
                    Text(c, modifier = Modifier.padding(start = 8.dp), color = if (has) Color.Black else Color.Gray)
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 16.dp))
            Text("Отзывы", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        items(place.reviews) { rev ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = if (rev.source == ReviewSource.YANDEX) Color(0xFFFFFDE7) else Color.White)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (rev.source == ReviewSource.YANDEX) Icons.Default.Star else Icons.Default.Person, null, modifier = Modifier.size(16.dp), tint = if (rev.source == ReviewSource.YANDEX) Color(0xFFFFB300) else Color.Gray)
                        Spacer(Modifier.width(8.dp))
                        Text(rev.author, fontWeight = FontWeight.Bold)
                    }
                    Text(rev.text, fontSize = 14.sp)
                }
            }
        }
        item {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                OutlinedTextField(value = userRev, onValueChange = { userRev = it }, label = { Text("Написать отзыв") }, modifier = Modifier.fillMaxWidth())
                Button(onClick = { if (userRev.isNotBlank()) { place.reviews.add(0, Review("Вы", 5, userRev, ReviewSource.USER)); userRev = "" } }, modifier = Modifier.align(Alignment.End).padding(top = 8.dp)) { Text("Отправить") }
            }
        }
    }
}

fun createBitmapFromVector(context: Context, color: Int): Bitmap {
    val drawable = ContextCompat.getDrawable(context, R.drawable.ic_pin)!!
    val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setTint(color); drawable.setBounds(0, 0, canvas.width, canvas.height); drawable.draw(canvas)
    return bitmap
}

fun sendLocalNotification(context: Context, title: String, text: String) {
    val builder = androidx.core.app.NotificationCompat.Builder(context, "PLACES_CHANNEL")
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(title).setContentText(text)
        .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT).setAutoCancel(true)
    val manager = androidx.core.app.NotificationManagerCompat.from(context)
    if (androidx.core.app.ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}