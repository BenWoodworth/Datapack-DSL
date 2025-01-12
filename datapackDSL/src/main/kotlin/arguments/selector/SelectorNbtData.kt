package arguments.selector

import arguments.Scores
import arguments.enums.Gamemode
import arguments.numbers.FloatRangeOrFloat
import arguments.numbers.IntRange
import arguments.numbers.Range
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonPrimitive
import net.benwoodworth.knbt.NbtCompound
import serializers.ToStringSerializer
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

@Serializable(GamemodeSelector.Companion.GamemodeSelectorSerializer::class)
class GamemodeSelector(var gamemode: Gamemode? = null, var invert: Boolean = false) {
	override fun toString() = when {
		gamemode == null -> ""
		invert -> "!${json.encodeToJsonElement(gamemode).jsonPrimitive.content}"
		else -> json.encodeToJsonElement(gamemode).jsonPrimitive.content
	}
	
	companion object {
		object GamemodeSelectorSerializer : ToStringSerializer<GamemodeSelector>()
	}
}

@Serializable(SelectorNbtData.Companion.SelectorNbtDataSerializer::class)
data class SelectorNbtData(
	var x: Double? = null,
	var y: Double? = null,
	var z: Double? = null,
	var dx: Double? = null,
	var dy: Double? = null,
	var dz: Double? = null,
	@SerialName("x_rotation")
	var xRotation: FloatRangeOrFloat? = null,
	@SerialName("y_rotation")
	var yRotation: FloatRangeOrFloat? = null,
	var distance: Range? = null,
	var limit: Int? = null,
	var level: IntRange? = null,
	var team: String? = null,
	var name: String? = null,
	var type: String? = null,
	var tag: String? = null,
	var nbtData: NbtCompound? = null,
	var advancements: Advancements? = null,
	var scores: Scores? = null,
	var sort: Sort? = null,
	var predicate: String? = null,
) {
	@SerialName("gamemode")
	private var _gamemode: GamemodeSelector = GamemodeSelector()
	
	@Transient
	var gamemode
		get() = _gamemode.gamemode
		set(value) {
			_gamemode.gamemode = value
		}
	
	fun advancements(block: AdvancementBuilder.() -> Unit) {
		val builder = AdvancementBuilder()
		builder.block()
		advancements = builder.build()
	}
	
	operator fun Gamemode.not(): Gamemode {
		_gamemode.invert = true
		return this
	}
	
	operator fun String.not() = "!$this"
	
	fun copyFrom(other: SelectorNbtData) {
		x = other.x
		y = other.y
		z = other.z
		dx = other.dx
		dy = other.dy
		dz = other.dz
		xRotation = other.xRotation
		yRotation = other.yRotation
		distance = other.distance
		limit = other.limit
		level = other.level
		team = other.team
		name = other.name
		type = other.type
		tag = other.tag
		nbtData = other.nbtData
		advancements = other.advancements
		scores = other.scores
		sort = other.sort
		predicate = other.predicate
		_gamemode = other._gamemode
	}
	
	companion object {
		object SelectorNbtDataSerializer : KSerializer<SelectorNbtData> {
			override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SelectorNbtData", PrimitiveKind.STRING)
			
			override fun deserialize(decoder: Decoder) = SelectorNbtData()
			
			override fun serialize(encoder: Encoder, value: SelectorNbtData) {
				val map = mutableMapOf<String, Any?>()
				value::class.memberProperties.forEach {
					it.isAccessible = true
					if (it.hasAnnotation<Transient>()) return@forEach
					
					val serialName = it.findAnnotation<SerialName>()?.value ?: it.name
					map[serialName] = it.getter.call(value)
				}
				
				encoder.encodeString(map.filter { it.value != null }.mapNotNull { (key, value) ->
					when (value) {
						is GamemodeSelector -> when (value.gamemode) {
							null -> return@mapNotNull null
							else -> "$key=${json.encodeToJsonElement(value).jsonPrimitive.content}"
						}
						
						is Advancements -> "$key=${json.encodeToJsonElement(value).jsonPrimitive.content}"
						is List<*> -> "$key=${json.encodeToJsonElement(value).jsonPrimitive.content}"
						is Map<*, *> -> "$key=${json.encodeToJsonElement(value).jsonPrimitive.content}"
						else -> "$key=$value"
					}
				}.joinToString(","))
			}
		}
	}
}
