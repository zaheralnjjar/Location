import React, { useEffect, useRef, useState } from 'react';
import L from 'leaflet';
import { Parking } from '../../types';
import LoadingSpinner from '../LoadingSpinner';
import Header from '../Header';

// Fix for default Leaflet icon path issue
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
});


interface MapScreenProps {
  parkings: Parking[];
  onAddParking: (coords: { lat: number; lng: number }) => void;
}

const MapScreen: React.FC<MapScreenProps> = ({ parkings, onAddParking }) => {
  const mapContainerRef = useRef<HTMLDivElement>(null);
  const mapRef = useRef<L.Map | null>(null);
  const [loading, setLoading] = useState(true);
  const [currentLocation, setCurrentLocation] = useState<L.LatLng | null>(null);

  const centerOnUser = () => {
    if (currentLocation && mapRef.current) {
        mapRef.current.setView(currentLocation, 16);
    } else {
        // Try to get location again if not available
        navigator.geolocation.getCurrentPosition(
            (position) => {
                const { latitude, longitude } = position.coords;
                const userLatLng = L.latLng(latitude, longitude);
                setCurrentLocation(userLatLng);
                if (mapRef.current) {
                    mapRef.current.setView(userLatLng, 16);
                }
            },
            () => console.error("Could not get location"),
            { enableHighAccuracy: true }
        );
    }
  };

  useEffect(() => {
    if (mapContainerRef.current && !mapRef.current) {
      const map = L.map(mapContainerRef.current).setView([24.7136, 46.6753], 13); // Default to Riyadh
      mapRef.current = map;

      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
      }).addTo(map);

      map.on('click', (e) => {
        L.popup()
          .setLatLng(e.latlng)
          .setContent(`
            <p>إضافة موقف هنا؟</p>
            <button id="add-parking-btn" class="w-full text-center mt-2 p-2 bg-primary-500 text-white rounded-md">إضافة</button>
          `)
          .openOn(map);
        
        const btn = document.getElementById('add-parking-btn');
        if (btn) {
            btn.onclick = () => {
                onAddParking(e.latlng);
                map.closePopup();
            };
        }
      });
      
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const { latitude, longitude } = position.coords;
          const userLatLng = L.latLng(latitude, longitude);
          setCurrentLocation(userLatLng);
          map.setView(userLatLng, 16);
          L.marker(userLatLng, {
            icon: L.divIcon({
                className: 'user-location-marker',
                html: '<div class="w-4 h-4 bg-blue-500 rounded-full border-2 border-white shadow-md"></div>',
                iconSize: [16, 16],
                iconAnchor: [8, 8]
            })
          }).addTo(map).bindPopup("موقعك الحالي");
          setLoading(false);
        },
        () => {
          console.error("Geolocation is not supported by this browser or permission denied.");
          setLoading(false);
        },
        { enableHighAccuracy: true }
      );
    }

    // Add parking markers
    if (mapRef.current) {
        // Clear existing markers before adding new ones
        mapRef.current.eachLayer((layer) => {
            if (layer instanceof L.Marker && !(layer.options.icon instanceof L.DivIcon)) {
                mapRef.current?.removeLayer(layer);
            }
        });

        parkings.forEach(parking => {
            L.marker([parking.latitude, parking.longitude])
            .addTo(mapRef.current!)
            .bindPopup(`<b>${parking.name}</b><br>${parking.address}`);
        });
    }

    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [parkings]);

  return (
    <div className="h-full flex flex-col">
       <Header title="الخريطة التفاعلية" icon="fa-map-location-dot" />
       {loading && <div className="absolute inset-0 flex items-center justify-center bg-white dark:bg-gray-900 z-20"><LoadingSpinner text="جاري تحديد الموقع..." /></div>}
       <div ref={mapContainerRef} className="flex-grow z-10" style={{ minHeight: 'calc(100vh - 120px)' }}></div>
       <button onClick={centerOnUser} className="absolute bottom-20 mb-2 left-4 z-20 w-12 h-12 bg-white dark:bg-gray-700 rounded-full shadow-lg flex items-center justify-center text-gray-700 dark:text-gray-200 text-xl">
            <i className="fa-solid fa-location-crosshairs"></i>
       </button>
    </div>
  );
};

export default MapScreen;