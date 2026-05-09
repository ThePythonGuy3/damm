# CapacityChecker — Contract for Team B
**Damm Smart Truck · what to implement, what to return**

This document is the entire contract for the function the routing team needs from Team B. Implement one Java method on the `bomboclot.routing.CapacityChecker` interface; the routing team calls it once per candidate trip.

---

## The function to implement

```java
package bomboclot.routing;

import java.util.List;

public interface CapacityChecker
{
    CapacityReport check(String date, String route_code, List<String> stop_order);
}
```

The `CapacityReport` record (also in `bomboclot.routing`) is already defined — you do not need to create it, only populate it.

---

## What we pass to you

| Parameter | Example | Meaning |
|---|---|---|
| `date` | `"08/05/2026"` | Delivery date in `dd/MM/yyyy` format. Same format used in `Detalle entrega.FECHA`. |
| `route_code` | `"DR0027"` | The route id, which determines the truck assigned for the day. |
| `stop_order` | `["828482327", "828482329", ...]` | List of `Entrega` ids in the order the routing team proposes to visit them. Order matters — returnables picked up early in the route shift the cumulative load curve. |

---

## What you return

A `CapacityReport` with seven fields. **All seven must be populated on every call**, even when `fits` is `true`.

| Field | Type | What to put in it |
|---|---|---|
| `fits` | `boolean` | `true` if all stops fit in one truck-trip, `false` otherwise. |
| `usedKilograms` | `double` | Total weight the load (deliveries + returnables) imposes on the truck, in kg. |
| `maxKilograms` | `double` | Maximum payload of the truck assigned to this route, in kg. |
| `usedLitres` | `double` | Total volume occupied, in litres. |
| `maxLitres` | `double` | Cargo bay capacity of the truck, in litres. |
| `overflowStopIds` | `List<String>` | When `fits=false`, the `Entrega` ids of the stops that should move to a separate trip. **Empty list when `fits=true`**. |
| `message` | `String` | Short human-readable diagnostic for the demo. Empty string if you have nothing useful to say. |

---

## Behaviour rules

**When `fits = true`** — `overflowStopIds` is an empty list. The routing team accepts the tour as-is and writes it to the output JSON. The utilisation fields (`usedKilograms`, `usedLitres`) are still rendered in the JSON so the demo can show "this truck is 78 % full."

**When `fits = false`** — `overflowStopIds` must be **non-empty** and must be a **strict subset** of `stop_order`. The routing team will remove those stops from the current trip, replan the remainder, and recursively call your function again with just the overflow as the new candidate. If you return `false` with an empty overflow list (or one that includes all stops), we throw an `IllegalStateException` because we cannot recover from "doesn't fit, but I can't tell you what to drop."

**When a single stop alone exceeds truck capacity** — set `fits = false`, put just that one Entrega in `overflowStopIds`, and put a clear `message` like `"single oversize client: 828482327"`. We will surface it loudly and stop trying to split.

**Multiple trucks is fine.** Two trips for one route is normal operation at DDI; the historical data shows drivers running up to nine transports on the same route on the same day. Splitting is not a failure.

---

## Example responses

A successful call where everything fits:

```java
return new CapacityReport(
    true,                      // fits
    4720.0, 6000.0,            // 4720 kg used out of 6000 kg
    24300.0, 30000.0,          // 24300 L used out of 30000 L
    List.of(),                 // no overflow
    ""                         // no message needed
);
```

A call where two stops need to move to a second trip:

```java
return new CapacityReport(
    false,
    7100.0, 6000.0,
    32000.0, 30000.0,
    List.of("828482329", "828482336"),
    "weight 7100 kg exceeds truck max 6000 kg; dropped 2 heaviest stops"
);
```

---

## Where to find the data you need

The `bomboclot.input.IReader` (already loaded in `Main`) gives you everything:

- `reader.get_deliveries(date, route_code)` returns the same `List<Delivery>` the routing team uses; each `Delivery` has its `customer_identifier` and the full list of `DeliveryLine` objects (`product_name`, `unit`, `quantity`).
- `reader.get_product(product_name)` returns a `Product` with the per-unit `Dimensions`, so you can compute per-line weight and volume.
- `reader.get_costumer(customer_identifier)` returns the `Costumer` if you need it (probably not for capacity).

The truck's `maxKilograms` and `maxLitres` are not yet exposed by `IReader` — for now you can hardcode reasonable defaults (~6000 kg, ~30000 L for a typical DDI distribution truck) or add a fourth method on `IReader` to look up the truck assigned to a route. Either is fine.

---

## A working stub for the routing team to wire against

Until your real implementation is ready, this one-liner stub keeps the pipeline running end-to-end. It currently lives in `Main.java`:

```java
CapacityChecker capacity = (d, r, ids) ->
        new CapacityReport(true, 0, 6000, 0, 30000, List.of(), "stub");
```

Replace it with `CapacityChecker capacity = new YourImplementation(reader);` once you ship.
