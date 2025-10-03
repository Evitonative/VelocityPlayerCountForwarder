# VelocityPlayerCountForwarder
A very simple (and somewhat hacky) Velocity plugin that forwards the player count (current and max) from the fist available in the forced host list.
If the host is not in the forced host list, it falls back to the configured connection attempt order.
If no backend responds, the proxy falls back to Velocity’s default values.

## Should you use this in production?
Probably not. This plugin blocks briefly while pinging backends and is only safe if you expect very few players or light load.