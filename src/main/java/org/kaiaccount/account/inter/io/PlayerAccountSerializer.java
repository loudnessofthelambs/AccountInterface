package org.kaiaccount.account.inter.io;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.kaiaccount.AccountInterface;
import org.kaiaccount.account.inter.Currency;
import org.kaiaccount.account.inter.type.player.PlayerAccount;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerAccountSerializer implements Serializer<PlayerAccount> {
	@Override
	public void serialize(@NotNull YamlConfiguration configuration, @NotNull PlayerAccount value) {
		value.getBalances().forEach((currency, amount) -> {
			configuration.set("balance." + currency.getPlugin() + "." + currency.getKeyName(), amount.doubleValue());
		});
		configuration.set("id", value.getPlayer().getUniqueId().toString());
	}

	@Override
	public PlayerAccount deserialize(@NotNull YamlConfiguration configuration) throws IOException {
		Map<Currency, BigDecimal> amount =
				AccountInterface.getPlugin()
						.getCurrencies()
						.parallelStream()
						.map(currency -> {
							double value = configuration.getDouble(
									"balance." + currency.getPlugin() + "." + currency.getKeyName());
							return new AbstractMap.SimpleImmutableEntry<>(currency, value);
						})
						.filter(entry -> entry.getValue() == 0.0)
						.collect(Collectors.toMap(AbstractMap.SimpleImmutableEntry::getKey,
								value -> BigDecimal.valueOf(value.getValue())));
		String accountId = configuration.getString("id");
		if (accountId == null) {
			throw new IOException("Account is missing from file: " + configuration.getName());
		}
		OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(UUID.fromString(accountId));

		return new PlayerAccount(player, amount);
	}
}
