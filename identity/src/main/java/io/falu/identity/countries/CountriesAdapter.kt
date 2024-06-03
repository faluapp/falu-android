package io.falu.identity.countries

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import coil.decode.SvgDecoder
import coil.load
import io.falu.identity.api.models.country.SupportedCountry
import io.falu.identity.databinding.ListItemCountriesBinding

internal class CountriesAdapter(context: Context, layoutId: Int, private val countries: List<SupportedCountry>) :
    ArrayAdapter<SupportedCountry>(context, layoutId) {

    override fun getCount(): Int {
        return countries.size
    }

    override fun getItem(position: Int): SupportedCountry? {
        return countries.getOrNull(position)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: ListItemCountriesBinding
        val view: View

        if (convertView == null) {
            val inflater = LayoutInflater.from(context)
            binding = ListItemCountriesBinding.inflate(inflater, parent, false)
            view = binding.root
            view.tag = binding
        } else {
            view = convertView
            binding = view.tag as ListItemCountriesBinding
        }

        val country = getItem(position)

        if (country != null) {
            binding.ivFlag.load(country.country.flag) {
                decoderFactory(SvgDecoder.Factory())
            }
            binding.tvCountry.text = country.country.name
        }

        return view
    }
}