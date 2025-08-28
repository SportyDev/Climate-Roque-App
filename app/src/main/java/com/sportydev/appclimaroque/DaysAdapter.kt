package com.sportydev.appclimaroque
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class DaysAdapter(
    private var days: List<Day>,
    private val onDayClick: (Day, Int) -> Unit
) : RecyclerView.Adapter<DaysAdapter.DayViewHolder>() {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = days[position]
        holder.bind(day, position)
    }

    override fun getItemCount(): Int = days.size

    fun updateDays(newDays: List<Day>) {
        days = newDays
        // Buscar el día seleccionado para mantener la posición (solo si es seleccionable)
        selectedPosition = days.indexOfFirst { it.isSelected && it.isSelectable }

        // Si no hay día seleccionado válido, buscar el día de hoy
        if (selectedPosition == -1) {
            selectedPosition = days.indexOfFirst { it.isToday }
        }

        notifyDataSetChanged()
    }

    fun selectDay(position: Int) {
        val oldPosition = selectedPosition
        selectedPosition = position
        notifyItemChanged(oldPosition)
        notifyItemChanged(selectedPosition)
    }

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.cardDay)
        private val txtDayNumber: TextView = itemView.findViewById(R.id.txtDayNumber)
        private val txtDayName: TextView = itemView.findViewById(R.id.txtDayName)

        fun bind(day: Day, position: Int) {
            txtDayNumber.text = day.dayNumber.toString()
            txtDayName.text = day.dayName

            // Configurar colores según el estado del día
            when {
                !day.isSelectable -> {
                    // Días futuros: gris claro y no clickeable
                    cardView.setCardBackgroundColor(
                        ContextCompat.getColor(itemView.context, R.color.disabled_gray)
                    )
                    txtDayNumber.setTextColor(
                        ContextCompat.getColor(itemView.context, R.color.disabled_text_gray)
                    )
                    txtDayName.setTextColor(
                        ContextCompat.getColor(itemView.context, R.color.disabled_text_gray)
                    )
                    itemView.isClickable = false
                    itemView.isFocusable = false
                    cardView.foreground = null // Remover el efecto ripple
                }
                day.isToday -> {
                    // Día de hoy: azul fuerte (tiene prioridad sobre seleccionado)
                    cardView.setCardBackgroundColor(
                        ContextCompat.getColor(itemView.context, R.color.today_blue)
                    )
                    txtDayNumber.setTextColor(
                        ContextCompat.getColor(itemView.context, android.R.color.white)
                    )
                    txtDayName.setTextColor(
                        ContextCompat.getColor(itemView.context, android.R.color.white)
                    )
                    itemView.isClickable = true
                    itemView.isFocusable = true
                    cardView.foreground = ContextCompat.getDrawable(itemView.context,
                        android.R.drawable.list_selector_background)
                }
                day.isSelected || position == selectedPosition -> {
                    // Día seleccionado: azul claro
                    cardView.setCardBackgroundColor(
                        ContextCompat.getColor(itemView.context, R.color.selected_blue)
                    )
                    txtDayNumber.setTextColor(
                        ContextCompat.getColor(itemView.context, android.R.color.white)
                    )
                    txtDayName.setTextColor(
                        ContextCompat.getColor(itemView.context, android.R.color.white)
                    )
                    itemView.isClickable = true
                    itemView.isFocusable = true
                    cardView.foreground = ContextCompat.getDrawable(itemView.context,
                        android.R.drawable.list_selector_background)
                }
                else -> {
                    // Día normal: blanco
                    cardView.setCardBackgroundColor(
                        ContextCompat.getColor(itemView.context, android.R.color.white)
                    )
                    txtDayNumber.setTextColor(
                        ContextCompat.getColor(itemView.context, android.R.color.black)
                    )
                    txtDayName.setTextColor(
                        ContextCompat.getColor(itemView.context, android.R.color.darker_gray)
                    )
                    itemView.isClickable = true
                    itemView.isFocusable = true
                    cardView.foreground = ContextCompat.getDrawable(itemView.context,
                        android.R.drawable.list_selector_background)
                }
            }

            // Solo configurar el click listener si el día es seleccionable
            if (day.isSelectable) {
                itemView.setOnClickListener {
                    onDayClick(day, position)
                    selectDay(position)
                }
            } else {
                itemView.setOnClickListener(null)
            }
        }
    }
}