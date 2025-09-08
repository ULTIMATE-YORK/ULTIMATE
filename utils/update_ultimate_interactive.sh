#!/bin/bash

# ULTIMATE Branch Update System - Interactive Edition
# Created for easy branch switching and updates

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Configuration
ULTIMATE_DIR="$HOME/workspace/ULTIMATE_MODEL_MANAGER"
BACKUP_DIR="$HOME/workspace/backups"
GITHUB_REPO="https://github.com/ultimate-pa/ultimate.git"

# Create backup directory if it doesn't exist
mkdir -p "$BACKUP_DIR"

# Function to print colored output
print_header() {
    echo
    echo -e "${BLUE}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║${NC}           ${PURPLE}ULTIMATE Branch Update System${NC}                ║"
    echo -e "${BLUE}║${NC}                  ${CYAN}Interactive Edition${NC}                        ║"
    echo -e "${BLUE}╚══════════════════════════════════════════════════════════════╝${NC}"
    echo
}

# Function to show spinner
show_spinner() {
    local pid=$1
    local delay=0.1
    local spinstr='|/-\'
    while [ "$(ps a | awk '{print $1}' | grep $pid)" ]; do
        local temp=${spinstr#?}
        printf " [%c]  " "$spinstr"
        local spinstr=$temp${spinstr%"$temp"}
        sleep $delay
        printf "\b\b\b\b\b\b"
    done
    printf "    \b\b\b\b"
}

# Function to check if ULTIMATE directory exists
check_ultimate_installation() {
    if [ -d "$ULTIMATE_DIR" ]; then
        return 0
    else
        return 1
    fi
}

# Function to get current branch
get_current_branch() {
    if [ -d "$ULTIMATE_DIR/.git" ]; then
        cd "$ULTIMATE_DIR"
        git branch --show-current 2>/dev/null || echo "unknown"
    else
        echo "not-installed"
    fi
}

# Function to create backup
create_backup() {
    if check_ultimate_installation; then
        local backup_name="ultimate_backup_$(date +%Y%m%d_%H%M%S)"
        echo -e "${YELLOW}Creating backup: $backup_name${NC}"
        
        (cp -r "$ULTIMATE_DIR" "$BACKUP_DIR/$backup_name") &
        show_spinner $!
        
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✓ Backup created successfully${NC}"
            echo "$backup_name" > "$BACKUP_DIR/latest_backup"
            return 0
        else
            echo -e "${RED}✗ Failed to create backup${NC}"
            return 1
        fi
    else
        echo -e "${YELLOW}⚠ No ULTIMATE installation found to backup${NC}"
        return 0
    fi
}

# Function to list available branches
list_branches() {
    echo -e "${CYAN}Fetching available branches...${NC}"
    
    if [ -d "$ULTIMATE_DIR/.git" ]; then
        cd "$ULTIMATE_DIR"
        git fetch origin >/dev/null 2>&1 &
        show_spinner $!
        
        echo -e "\n${CYAN}Available branches:${NC}"
        git branch -r | grep -v HEAD | sed 's/origin\///' | sort | head -20
        echo -e "${YELLOW}... (showing first 20 branches)${NC}"
    else
        echo -e "${RED}✗ ULTIMATE not installed or not a git repository${NC}"
    fi
}

# Function to validate branch exists
validate_branch() {
    local branch=$1
    if [ -d "$ULTIMATE_DIR/.git" ]; then
        cd "$ULTIMATE_DIR"
        git ls-remote --heads origin "$branch" | grep -q "$branch"
        return $?
    else
        return 1
    fi
}

# Function to update to specific branch
update_to_branch() {
    local branch=$1
    
    echo -e "${CYAN}Updating ULTIMATE to branch: ${YELLOW}$branch${NC}"
    
    # Validate branch exists
    echo -e "${CYAN}Validating branch...${NC}"
    if ! validate_branch "$branch"; then
        echo -e "${RED}✗ Branch '$branch' not found${NC}"
        echo -e "${YELLOW}Would you like to see available branches? (y/n):${NC} "
        read -r show_branches
        if [[ $show_branches =~ ^[Yy]$ ]]; then
            list_branches
        fi
        return 1
    fi
    
    # Create backup
    if ! create_backup; then
        echo -e "${RED}✗ Backup failed, aborting update${NC}"
        return 1
    fi
    
    # Navigate to ULTIMATE directory
    cd "$ULTIMATE_DIR" || {
        echo -e "${RED}✗ Cannot access ULTIMATE directory${NC}"
        return 1
    }
    
    # Fetch latest changes
    echo -e "${CYAN}Fetching latest changes...${NC}"
    (git fetch origin) >/dev/null 2>&1 &
    show_spinner $!
    
    # Switch to branch
    echo -e "${CYAN}Switching to branch $branch...${NC}"
    (git checkout "$branch" && git reset --hard "origin/$branch") >/dev/null 2>&1 &
    show_spinner $!
    
    if [ $? -ne 0 ]; then
        echo -e "${RED}✗ Failed to switch to branch $branch${NC}"
        return 1
    fi
    
    # Build ULTIMATE
    echo -e "${CYAN}Building ULTIMATE...${NC}"
    (mvn clean compile) >/dev/null 2>&1 &
    show_spinner $!
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ ULTIMATE successfully updated to branch: $branch${NC}"
        
        # Show current status
        echo -e "\n${CYAN}Current Installation:${NC}"
        echo -e "  ${CYAN}Branch:${NC} $(git branch --show-current)"
        echo -e "  ${CYAN}Commit:${NC} $(git rev-parse HEAD | cut -c1-8)"
        echo -e "  ${CYAN}Date:${NC} $(git log -1 --pretty=%ad --date=short)"
        
        return 0
    else
        echo -e "${RED}✗ Build failed${NC}"
        return 1
    fi
}

# Function to restore from backup
restore_from_backup() {
    if [ ! -f "$BACKUP_DIR/latest_backup" ]; then
        echo -e "${RED}✗ No backup found${NC}"
        return 1
    fi
    
    local backup_name=$(cat "$BACKUP_DIR/latest_backup")
    if [ ! -d "$BACKUP_DIR/$backup_name" ]; then
        echo -e "${RED}✗ Backup directory not found: $backup_name${NC}"
        return 1
    fi
    
    echo -e "${YELLOW}Restoring from backup: $backup_name${NC}"
    echo -e "${YELLOW}This will overwrite current installation. Continue? (y/n):${NC} "
    read -r confirm
    
    if [[ $confirm =~ ^[Yy]$ ]]; then
        rm -rf "$ULTIMATE_DIR"
        (cp -r "$BACKUP_DIR/$backup_name" "$ULTIMATE_DIR") &
        show_spinner $!
        
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✓ Successfully restored from backup${NC}"
        else
            echo -e "${RED}✗ Failed to restore from backup${NC}"
        fi
    else
        echo -e "${YELLOW}Restore cancelled${NC}"
    fi
}

# Function to handle errors
handle_error() {
    echo -e "${RED}✗ An error occurred. Check the output above for details.${NC}"
    exit 1
}

# Main function
main() {
    print_header
    
    # Show current status
    echo -e "${CYAN}Current Installation:${NC}"
    if check_ultimate_installation; then
        local current_branch=$(get_current_branch)
        if [ "$current_branch" != "not-installed" ]; then
            cd "$ULTIMATE_DIR"
            echo -e "  ${CYAN}Branch:${NC} $current_branch"
            echo -e "  ${CYAN}Commit:${NC} $(git rev-parse HEAD 2>/dev/null | cut -c1-8 || echo 'unknown')"
            echo -e "  ${CYAN}Status:${NC} ${GREEN}Installed${NC}"
        else
            echo -e "  ${YELLOW}ULTIMATE installed but not a git repository${NC}"
        fi
    else
        echo -e "  ${RED}ULTIMATE not installed${NC}"
    fi
    
    echo
    echo -e "${CYAN}What would you like to do?${NC}"
    echo -e "  ${YELLOW}1)${NC} Update to specific branch/tag"
    echo -e "  ${YELLOW}2)${NC} List available branches"
    echo -e "  ${YELLOW}3)${NC} Show current installation info"
    echo -e "  ${YELLOW}4)${NC} Restore from backup"
    echo -e "  ${YELLOW}5)${NC} Exit"
    echo
    echo -n -e "${CYAN}Enter your choice (1-5): ${NC}"
    read -r choice
    
    case $choice in
        1)
            if ! check_ultimate_installation; then
                echo -e "${RED}✗ ULTIMATE not installed${NC}"
                exit 1
            fi
            
            echo -n -e "${CYAN}Enter branch/tag name: ${NC}"
            read -r branch_name
            
            if [ -z "$branch_name" ]; then
                echo -e "${RED}✗ Branch name cannot be empty${NC}"
                exit 1
            fi
            
            echo -e "${YELLOW}About to update to branch: $branch_name${NC}"
            echo -n -e "${YELLOW}Continue? (yes/no): ${NC}"
            read -r confirm
            
            if [[ $confirm == "yes" ]]; then
                update_to_branch "$branch_name"
            else
                echo -e "${YELLOW}Update cancelled${NC}"
            fi
            ;;
            
        2)
            list_branches
            ;;
            
        3)
            if check_ultimate_installation && [ -d "$ULTIMATE_DIR/.git" ]; then
                cd "$ULTIMATE_DIR"
                echo -e "\n${CYAN}Detailed Installation Info:${NC}"
                echo -e "${CYAN}Directory:${NC} $ULTIMATE_DIR"
                echo -e "${CYAN}Branch:${NC} $(git branch --show-current)"
                echo -e "${CYAN}Commit:${NC} $(git rev-parse HEAD)"
                echo -e "${CYAN}Author:${NC} $(git log -1 --pretty=%an)"
                echo -e "${CYAN}Date:${NC} $(git log -1 --pretty=%ad)"
                echo -e "${CYAN}Message:${NC} $(git log -1 --pretty=%B)"
            else
                echo -e "${YELLOW}ULTIMATE not installed${NC}"
            fi
            ;;
            
        4)
            restore_from_backup
            ;;
            
        5)
            echo -e "${YELLOW}Goodbye!${NC}"
            exit 0
            ;;
            
        *)
            echo -e "${RED}Invalid option${NC}"
            exit 1
            ;;
    esac
}

# Trap errors
trap 'handle_error' ERR

# Run main function
main
