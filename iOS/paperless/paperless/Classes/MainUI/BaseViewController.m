//
//  BaseViewController.m
//  paperless
//
//  Created by Matt Smollinger on 11/3/12.
//  Copyright (c) 2012 Skaffl. All rights reserved.
//

#import "BaseViewController.h"
#import "FileView.h"

@interface BaseViewController ()

@property (nonatomic, strong) FileListViewController *fileList;
@property (nonatomic, strong) UIViewController *detailView;
@property (nonatomic, strong) UINavigationController *navController;
@property (nonatomic, strong) NSMutableDictionary *fileViewDictionary;

@end

@implementation BaseViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self)
    {
        // Custom initialization
        self.fileList = [[FileListViewController alloc] initWithStyle:UITableViewStylePlain];
        self.fileList.delegate = self;
        self.navController = [[UINavigationController alloc] initWithRootViewController:self.fileList];
        self.detailView = [[UIViewController alloc] init];
        self.detailView.view.backgroundColor = [UIColor whiteColor];
        UIImageView *logo = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"rung_logo_2.png"]];
        logo.autoresizingMask = UIViewAutoresizingFlexibleTopMargin;
        logo.center = self.detailView.view.center;
        [self.detailView.view addSubview:logo]; 
        self.viewControllers = @[self.navController, self.detailView];
        self.fileViewDictionary = [[NSMutableDictionary alloc] initWithCapacity:5.0];
        
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - FileListDelegate

- (void)didSelectDetailItemAtIndexPath:(NSIndexPath *)indexPath
{
    NSString *key;
    if (indexPath.row == 0 || indexPath.row == 1)
    {
        key = [NSString stringWithFormat:@"%@%d", @"assignment", 0];
    }
    else
    {
        key = [NSString stringWithFormat:@"%@%d", @"assignment", indexPath.row];
    }
    FileView *detailView = [self.fileViewDictionary objectForKey:key];
    if (detailView == nil)
    {
        detailView = [[FileView alloc] init];
    }
    if (indexPath.row == 0)
    {
        detailView.thePenMode = TeacherView;
    }
    NSArray *newVCs = [NSArray arrayWithObjects:[self.viewControllers objectAtIndex:0], detailView, nil];
    self.viewControllers = newVCs;
    [self.fileViewDictionary setObject:detailView forKey:key];
}

@end
