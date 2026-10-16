-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Oct 16, 2026 at 11:01 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `banking_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `accounts`
--

CREATE TABLE `accounts` (
  `id` bigint(20) NOT NULL,
  `account_number` varchar(34) NOT NULL,
  `account_name` varchar(150) NOT NULL,
  `balance` decimal(19,4) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `accounts`
--

INSERT INTO `accounts` (`id`, `account_number`, `account_name`, `balance`, `created_at`) VALUES
(5, '1001', 'Bill Angelo', 403.0000, '2026-10-16 19:56:45'),
(6, '1002', 'Earl Susana', 3100.0000, '2026-10-16 19:24:38'),
(7, '1003', 'Julian De Guzman', 10600.0000, '2026-10-16 19:56:45'),
(8, '1004', 'Kurt Atencia', 17500.0000, '2026-10-16 19:25:00'),
(9, '2783', 'Andrew Atup', 50000.0000, '2026-10-16 19:22:03'),
(10, '52984', 'Summer Padilla', 20000.0000, '2026-10-16 19:23:41'),
(11, '5512', 'Bryan Andrew', 4000.0000, '2026-10-16 19:28:25');

-- --------------------------------------------------------

--
-- Table structure for table `transactions`
--

CREATE TABLE `transactions` (
  `id` bigint(20) NOT NULL,
  `transaction_reference` varchar(50) NOT NULL,
  `account_number` varchar(34) NOT NULL,
  `transaction_type` varchar(20) NOT NULL,
  `amount` decimal(19,4) NOT NULL,
  `balance` decimal(19,4) NOT NULL,
  `reference_number` varchar(34) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `remarks` varchar(255) DEFAULT NULL,
  `related_account_number` varchar(34) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `transactions`
--

INSERT INTO `transactions` (`id`, `transaction_reference`, `account_number`, `transaction_type`, `amount`, `balance`, `reference_number`, `created_at`, `remarks`, `related_account_number`) VALUES
(4, 'TXN202610170323410004', '52984', 'WITHDRAW', 5000.0000, 20000.0000, 'TXN202610170323410004', '2026-10-16 19:23:41', 'Cash withdrawal', NULL),
(5, 'TXN202610170324040005', '1001', 'WITHDRAW', 1000.0000, 0.0000, 'TXN202610170324040005', '2026-10-16 19:24:04', 'Cash withdrawal', NULL),
(6, 'TXN202610170324270006', '1001', 'DEPOSIT', 5000.0000, 5000.0000, 'TXN202610170324270006', '2026-10-16 19:24:27', 'Cash deposit', NULL),
(7, 'TXN202610170324380007', '1001', 'TRANSFER_OUT', 2500.0000, 2500.0000, 'TXN202610170324380007', '2026-10-16 19:24:38', 'Transfer to 1002', '1002'),
(8, 'TXN202610170324380008', '1002', 'TRANSFER_IN', 2500.0000, 3100.0000, 'TXN202610170324380008', '2026-10-16 19:24:38', 'Transfer from 1001', '1001'),
(9, 'TXN202610170324450009', '1001', 'DEPOSIT', 1003.0000, 3503.0000, 'TXN202610170324450009', '2026-10-16 19:24:45', 'Cash deposit', NULL),
(10, 'TXN202610170325000010', '1001', 'TRANSFER_OUT', 2500.0000, 1003.0000, 'TXN202610170325000010', '2026-10-16 19:25:00', 'Transfer to 1004', '1004'),
(11, 'TXN202610170325000011', '1004', 'TRANSFER_IN', 2500.0000, 17500.0000, 'TXN202610170325000011', '2026-10-16 19:25:00', 'Transfer from 1001', '1001'),
(12, 'TXN202610170347510001', '1001', 'DEPOSIT', 5000.0000, 6003.0000, 'TXN202610170347510001', '2026-10-16 19:47:51', 'Cash deposit', NULL),
(13, 'TXN202610170353340001', '1001', 'WITHDRAW', 5000.0000, 1003.0000, 'TXN202610170353340001', '2026-10-16 19:53:34', 'Cash withdrawal', NULL),
(14, 'TXN202610170356450002', '1001', 'TRANSFER_OUT', 600.0000, 403.0000, 'TXN202610170356450002', '2026-10-16 19:56:45', 'Transfer to 1003', '1003'),
(15, 'TXN202610170356450003', '1003', 'TRANSFER_IN', 600.0000, 10600.0000, 'TXN202610170356450003', '2026-10-16 19:56:45', 'Transfer from 1001', '1001');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `accounts`
--
ALTER TABLE `accounts`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `account_number` (`account_number`);

--
-- Indexes for table `transactions`
--
ALTER TABLE `transactions`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `transaction_reference` (`transaction_reference`),
  ADD KEY `account_number` (`account_number`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `accounts`
--
ALTER TABLE `accounts`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `transactions`
--
ALTER TABLE `transactions`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `transactions`
--
ALTER TABLE `transactions`
  ADD CONSTRAINT `fk_transaction_account` FOREIGN KEY (`account_number`) REFERENCES `accounts` (`account_number`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
